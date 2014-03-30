package in.myrpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import in.myrpc.channel.ChannelClient;
import in.myrpc.model.ConnectRequest;
import in.myrpc.model.ConnectResponse;
import in.myrpc.model.ProvisionRequest;
import in.myrpc.model.ProvisionResponse;
import in.myrpc.model.RpcRelay;
import in.myrpc.model.RpcRequest;
import in.myrpc.model.RpcReturn;
import in.myrpc.model.RpcReturnRelay;
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
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.json.JSONException;

/**
 * class that gives access to the restful methods of MyRpc.in
 *
 * @author kguthrie
 */
public class MyRpc implements ChannelClient.ChannelListener {

    private static final String localApp = "http://localhost:8888";
    private static final String productionApp = "https://www.myrpc.in";

    private final ObjectMapper mapper;
    private final RpcMethodReflection interop;
    private String endpointLocator;
    private final AtomicInteger lastRequestId;
    private final String domain;

    private final Map<String, RpcCallback> callbacks;
    private ChannelClient channelCleint;

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
     * @throws java.io.IOException
     */
    public String provision(String name, String centerpointLocator)
            throws IOException {
        ProvisionRequest pr = new ProvisionRequest(name, centerpointLocator);
        String prString = mapper.writeValueAsString(pr);
        ProvisionResponse response = mapper.readValue(
                post("/r/provision/", prString), ProvisionResponse.class);
        endpointLocator = response.getEndpointLocator();
        return endpointLocator;
    }

    /**
     * Connect to the server as an endpoint
     *
     * @throws java.io.IOException
     */
    public void connect() throws IOException {
        ConnectRequest request = new ConnectRequest(endpointLocator);
        String rqString = mapper.writeValueAsString(request);
        String rspString = post("/r/connect", rqString);
        ConnectResponse response
                = mapper.readValue(rspString, ConnectResponse.class);
        String token = response.getChannelToken();

        URI uri = null;

        try {
            uri = new URI(domain);
        }
        catch (URISyntaxException e) {
            throw new IOException(e);
        }

        channelCleint = ChannelClient.createChannel(uri, token, this);

        try {
            channelCleint.open();
        }
        catch (ChannelClient.ChannelException e) {
            throw new IOException(e);
        }
        catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Close the current channel connection
     */
    public void close() {
        if (channelCleint == null) {
            return;
        }

        try {
            channelCleint.close();
        }
        catch (ChannelClient.ChannelException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * call the given method on the given endpoint target with the given
     * arguments
     *
     * @param targetEndpointLocator
     * @param method
     * @param arguments
     * @param callback
     * @throws java.io.IOException
     */
    public void call(String targetEndpointLocator, String method,
            Map<String, String> arguments, RpcCallback callback)
            throws IOException {

        String requestId = Integer.toHexString(lastRequestId.incrementAndGet());

        RpcRequest rpc = new RpcRequest(requestId,
                endpointLocator, targetEndpointLocator,
                method, arguments);

        if (callback != null) {
            callbacks.put(requestId, callback);
        }

        post("/r/pc", mapper.writeValueAsString(rpc));

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
            String requestId) throws IOException {

        RpcReturn result = new RpcReturn(requestId, value,
                originalSourceLocator, endpointLocator);
        String rpcReturnString = mapper.writeValueAsString(result);

        post("/r/pc/return", rpcReturnString);
    }

    /**
     * Post the given content to the given uri on MyRpc.in
     *
     * @param uri
     * @param content
     */
    private String post(String uri, String content) throws IOException {

        assert (uri != null);
        assert (content != null);

        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        }

        byte[] contentBytes = content.getBytes("UTF-8");
        URL url = new URL(domain + uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("charset", "utf-8");
        connection.setRequestProperty("Content-Length",
                "" + Integer.toString(contentBytes.length));
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

        return result.toString();
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
        catch (IOException ex) {
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
            channelCleint.close();
        }
        catch (ChannelClient.ChannelException ex) {
            ex.printStackTrace();
        }

        try {
            connect();
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
