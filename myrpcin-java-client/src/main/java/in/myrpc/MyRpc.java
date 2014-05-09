package in.myrpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import in.myrpc.model.ConnectRequest;
import in.myrpc.model.ConnectResponse;
import in.myrpc.model.ProvisionRequest;
import in.myrpc.model.ProvisionResponse;
import in.myrpc.model.RpcRelay;
import in.myrpc.model.RpcRequest;
import in.myrpc.model.RpcReturn;
import in.myrpc.model.RpcReturnRelay;
import in.myrpc.receiver.ChannelClient;
import in.myrpc.receiver.EvaluativeMessageReceiver;
import in.myrpc.receiver.MessageHandler;
import in.myrpc.receiver.MessageReceiver;
import in.myrpc.reflect.RpcMethodReflection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * class that gives access to the restful methods of MyRpc.in
 *
 * @author kguthrie
 */
public class MyRpc implements ChannelClient.ChannelListener, MessageHandler {

    private static final String localApp = "http://localhost:8888";
    private static final String productionApp = "https://www.myrpc.in";

    private final ObjectMapper mapper;
    private final RpcMethodReflection interop;
    private String endpointLocator;
    private final AtomicInteger lastRequestId;
    private final String domain;

    private final Map<String, RpcCallback> callbacks;

    private MessageReceiver receiver;

    private Timer reconnectAfterExpireTimer;

    public MyRpc(String endpointLocator, Object rpcMethodContainer,
            boolean local) {
        lastRequestId = new AtomicInteger(0);
        mapper = new ObjectMapper();
        this.endpointLocator = endpointLocator;
        this.interop = new RpcMethodReflection(rpcMethodContainer);
        this.callbacks = Maps.newHashMap();
        domain = local ? localApp : productionApp;

        ignoreUntrustedCertificateErrors();

        // Add a shutdown callback so that channels are closed when the jvm
        // exists
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                close();
            }
        }));
    }

    /**
     * Ignore the untrusted certificate errors. This should go away when we get
     * a big boy ssl cert
     */
    private void ignoreUntrustedCertificateErrors() {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs,
                        String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs,
                        String authType) {
                }
            }
        };

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    sslContext.getSocketFactory());
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Provision this client as an endpoint to the given
     *
     * @param name
     * @param centerpointLocator
     * @return endpoint locator
     * @throws in.myrpc.MyRpcException
     */
    public String provision(String name, String centerpointLocator)
            throws MyRpcException {
        ProvisionRequest pr = new ProvisionRequest(name, centerpointLocator);
        ProvisionResponse response = post("/r/provision/", pr,
                ProvisionResponse.class);
        endpointLocator = response.getEndpointLocator();
        return endpointLocator;
    }

    /**
     * Connect to the server as an endpoint
     *
     * @throws in.myrpc.MyRpcException
     */
    public void connect() throws MyRpcException {
        ConnectRequest request = new ConnectRequest(endpointLocator);
        ConnectResponse response = post("/r/connect", request,
                ConnectResponse.class);

        String token = response.getChannelToken();

        URI uri = null;

        try {
            uri = new URI(domain);
        }
        catch (URISyntaxException e) {
            throw new MyRpcException(e);
        }

        // channelClient = ChannelClient.createChannel(uri, token, this);

        receiver = new EvaluativeMessageReceiver(token, this);

        //try {
            // channelClient.open();
        receiver.open();
        //}
//        catch (ChannelClient.ChannelException e) {
//            throw new MyRpcException(e);
//        }


        // If the cahnnel was opened successfully, then set a timer to reconnect
        // after the lifetime of the channel passes

        if (reconnectAfterExpireTimer != null) {
            reconnectAfterExpireTimer.cancel();
        }

        reconnectAfterExpireTimer = new Timer();
        reconnectAfterExpireTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                onError(0, "Reconnect before timeout");
//                catch(ChannelClient.ChannelException ex) {
//                    throw new RuntimeException(ex);
//                }
//                catch(IOException ioe) {
//                    throw new RuntimeException(ioe);
//                }
            }
        }, response.getSecondsUntilExpire() * 1000);
    }

    /**
     * Close the current channel connection
     */
    public void close() {
        try {
            //ChannelClient oldClient = channelCleint;
            MessageReceiver oldReceiver = receiver;
            connect();
            //oldClient.close();
            oldReceiver.close();
        }
        catch (MyRpcException e) {
            throw new RuntimeException(e);
        }
//        if (channelCleint == null) {
//            return;
//        }
//
//        try {
//            channelCleint.close();
//        }
//        catch (ChannelClient.ChannelException ex) {
//            ex.printStackTrace();
//        }
    }

    /**
     * call the given method on the given endpoint target with the given
     * arguments
     *
     * @param targetEndpointLocator
     * @param method
     * @param arguments
     * @param callback
     * @throws in.myrpc.MyRpcException
     */
    public void call(String targetEndpointLocator, String method,
            Map<String, String> arguments, RpcCallback callback)
            throws MyRpcException {

        String requestId = Integer.toHexString(lastRequestId.incrementAndGet());

        RpcRequest rpc = new RpcRequest(requestId,
                endpointLocator, targetEndpointLocator,
                method, arguments);

        if (callback != null) {
            callbacks.put(requestId, callback);
        }

        post("/r/pc", rpc, null);

    }

    /**
     * Send the response to an rpc call back to the original sender with the
     * requestId, so that they know what request it corresponds to.
     *
     * @param originalSourceLocator
     * @param value
     * @param requestId
     */
    protected void returnCall(String originalSourceLocator, String value,
            String requestId) throws MyRpcException {

        RpcReturn result = new RpcReturn(requestId, value,
                originalSourceLocator, endpointLocator);

        post("/r/pc/return", result, null);
    }

    /**
     * Post the given content to the given uri on MyRpc.in
     *
     * @param uri
     * @param content
     */
    private <T> T post(String uri, Object content, Class<T> returnType)
            throws MyRpcException {

        assert (uri != null);
        assert (content != null);

        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        }

        try {

            byte[] contentBytes = mapper.writeValueAsBytes(content); //utf-8
            URL url = new URL(domain + uri);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length",
                    Integer.toString(contentBytes.length));
            connection.setUseCaches(false);

            OutputStream wr = connection.getOutputStream();
            wr.write(contentBytes);
            wr.flush();
            wr.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));

            String line;
            StringBuilder result = new StringBuilder();

            while ((line = in.readLine()) != null) {
                result.append(line);
                result.append("\n");
            }

            in.close();
            connection.disconnect();
            if (returnType != null) {
                return mapper.readValue(result.toString(), returnType);
            }
            else {
                return null;
            }
        }
        catch(IOException e) {
            throw new MyRpcException("Post to " + uri + " failed", e);
        }

    }

    /**
     * This method is called when an rpc relay is received by the channel
     * listener indicating an rpc call on this endpoing
     *
     * @param relay
     */
    protected void onRpcCall(RpcRelay relay) {
        assert (relay != null);

        String result;

        try {
            result = interop.call(relay.getMethod(), relay.getArguments());
        }
        catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            Throwable prev = null;
            Throwable curr = ex;

            while (curr != null) {

                if (prev != null) {
                    pw.println();
                    pw.println("Caused By");
                }

                pw.print(ex.getClass().getName());
                pw.print(": ");
                pw.println(ex.getMessage());
                ex.printStackTrace(pw);

                prev = curr;
                curr = ex.getCause();
            }

            pw.close();
            result = sw.toString();
        }

        try {
            returnCall(relay.getOriginLocator(), result, relay.getRequestId());
        }
        catch (MyRpcException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * This method is called when an rpc return comes back from another endpoint
     * on which this endpoint called an rpc method
     *
     * @param relay
     */
    protected void onRpcReturnCall(RpcReturnRelay relay) {
        assert (relay != null);

        RpcCallback callback = callbacks.get(relay.getRequestId());

        if (callback != null) {
            callback.onSuccss(relay.getReturnValue());
        }
    }

    /**
     * ******* Channel Callback methods after here ***********
     */
    @Override
    public void onOpen() {
        System.out.println("Channel opened");
    }

    /**
     * Called by the channel api whenever a message comes in
     *
     * @param message
     */
    @Override
    public void onMessage(String message) {
        try {
            RpcRelay relay = mapper.readValue(message, RpcRelay.class);
            onRpcCall(relay);
        }
        catch (IOException ex) {
            try {
                RpcReturnRelay relay
                        = mapper.readValue(message, RpcReturnRelay.class);
                onRpcReturnCall(relay);
            }
            catch (IOException ex2) {
                throw new RuntimeException(ex2);
            }
        }

    }

    @Override
    public void onClose() {
        System.out.println("Channel clonsed");
    }

    @Override
    public void onError(int code, String description) {
        System.out.println("Error " + code + " " + description);
        try {
                    //ChannelClient oldClient = channelCleint;
                    MessageReceiver oldReceiver = receiver;
                    connect();
                    //oldClient.close();
                    oldReceiver.close();
                }
                catch (MyRpcException e) {
                    throw new RuntimeException(e);
                }
//        try {
//            channelCleint.close();
//        }
//        catch (ChannelClient.ChannelException ex) {
//            ex.printStackTrace();
//        }
//
//        try {
//            connect();
//        }
//        catch (IOException ex) {
//            throw new RuntimeException(ex);
//        }
    }

}
