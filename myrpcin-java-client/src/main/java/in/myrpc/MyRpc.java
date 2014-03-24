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
import in.myrpc.model.RpcResponse;
import in.myrpc.reflect.RpcMethodReflection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONException;

/**
 * class that gives access to the restful methods of MyRpc.in
 *
 * @author kguthrie
 */
public class MyRpc implements ChannelClient.ChannelListener {

    private final ObjectMapper mapper;
    private final RpcMethodReflection interop;
    private String endpointLocator;
    private final AtomicInteger lastRequestId;

    private final Map<String, RpcCallback> callbacks;

    public MyRpc(String endpointLocator, Object rpcMethodContainer) {
        lastRequestId = new AtomicInteger(0);
        mapper = new ObjectMapper();
        this.endpointLocator = endpointLocator;
        this.interop = new RpcMethodReflection(rpcMethodContainer);
        this.callbacks = Maps.newHashMap();
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
            uri = new URI("http://localhost:8888");
        }
        catch (URISyntaxException e) {
            throw new IOException(e);
        }

        ChannelClient cc = ChannelClient.createChannel(uri, token, this);

        try {
            cc.open();
        }
        catch (ChannelClient.ChannelException e) {
            throw new IOException(e);
        }
        catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * call the given method on the given endpoint target with the given
     * arguments
     *
     * @param targetEndpointLocator
     * @param method
     * @param arguments
     * @throws java.io.IOException
     */
    public void call(String targetEndpointLocator, String method,
            Map<String, String> arguments, RpcCallback callback)
            throws IOException {

        String requestId = Integer.toHexString(lastRequestId.incrementAndGet());

        RpcRequest rpc = new RpcRequest(requestId,
                endpointLocator, targetEndpointLocator,
                method, arguments);

        String rpcResponseStr = post("/r/pc", mapper.writeValueAsString(rpc));
        RpcResponse rpcResponse = mapper.readValue(rpcResponseStr,
                RpcResponse.class);

        if (callback != null) {
            callbacks.put(requestId, callback);
        }
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

        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }

        byte[] contentBytes = content.getBytes("UTF-8");
        URL url = new URL("http://localhost:8888/" + uri);
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
     * ******* Channel Callback methods after here ***********
     */
    @Override
    public void onOpen() {
        System.out.println("Channel opened");
    }

    @Override
    public void onMessage(String message) {
        RpcRelay relay = null;

        try {
            relay = mapper.readValue(message, RpcRelay.class);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        assert (relay != null);

        String result = interop.call(relay.getMethod(), relay.getArguments());

        RpcCallback callback = callbacks.get(relay.getResponseId());

        if (callback != null) {
            callback.onSuccss(result);
        }
    }

    @Override
    public void onClose() {
        System.out.println("Channel clonsed");
    }

    @Override
    public void onError(int code, String description) {
        System.out.println("Error " + code + " " + description);
    }

}
