package in.myrpc.receiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * Super simple http client class.  This class has some basic methods that allow
 * it to act like a browser and perform get and post operations
 * @author kguthrie
 */
public class HttpClientProxy {

    private static final String acceptValue =
            "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

    private static final String acceptLanguageValue = "en-US,en;q=0.5";
    private static final String acceptEncodingValue = "";

    private static final String userAgent = "Mozilla/5.0 (Macintosh; "
            + "Intel Mac OS X 10.9; rv:26.0) Gecko/20100101 Firefox/26.0";

    private static final String postContentType =
            "application/x-www-form-urlencoded; charset=UTF-8";

    private final Map<String, ProxyContext> openContexts;
    private final CloseableHttpClient client;
    private final HttpClientContext globalContext;

    public HttpClientProxy() {
        globalContext = HttpClientContext.create();
        openContexts = new HashMap<String, ProxyContext>();
        client = HttpClients.custom()
                .setUserAgent(userAgent)
                .build();
    }



    /**
     * Gets either a persistent or temporary HttpClientProxyContent for the
     * given inputs
     *
     * @param url
     * @param referer
     * @param content
     * @return
     */
    private ProxyContext getContextFor(String contextName) throws IOException {

        ProxyContext result;

        if (contextName != null) {
            result = openContexts.get(contextName);

            if (result == null) {
                result = new ProxyContext(HttpClientContext.create());
                openContexts.put(contextName, result);
            }
        }
        else {
            result = new ProxyContext(null);
        }

        return result;
    }

    /**
     * Get a reader for the http response to the given url with content
     * @param url
     * @param referer
     * @param content
     * @param context
     * @return
     * @throws IOException
     */
    private BufferedReader getReader(String method, String url, String referer,
            String content, ProxyContext context) throws IOException {

        HttpRequestBase request;

        BufferedReader result = context.getConnection(method, url);

        if (result != null) {
            return result;
        }

        if (content == null) {
            request = new HttpGet(url);
        }
        else {
            request = new HttpPost(url);
            byte[] byteContent = content.getBytes();

            ((HttpPost)request).setEntity(
                    EntityBuilder.create()
                            .setBinary(byteContent).build());

            request.setHeader("Content-Type", postContentType);
            request.setHeader("Cache-Control", "no-cache");
            request.setHeader("Pragma", "no-cache");
        }

        request.setHeader("Accept", acceptValue);
        request.setHeader("Accept-Encoding", acceptEncodingValue);
        request.setHeader("Referer", referer);
        request.setHeader("Accept-Language", acceptLanguageValue);

        CloseableHttpResponse response;

        response = client.execute(request, globalContext);

        HttpEntity entity = response.getEntity();

        result = new BufferedReader(new InputStreamReader(
                entity.getContent()));

        context.storeConnection(method, url, result, response);

        return result;
    }

    /**
     * Read the given amount from the given url within the given context
     * @param url
     * @param referer
     * @param content
     * @param maxLines
     * @param minBytes
     * @param contextName
     * @return
     * @throws IOException
     */
    private StringBuilder readFromUrl(String url, String referer,
            String content, int maxLines, int minBytes, String contextName)
                    throws IOException {

        String method = content == null
                ? HttpGet.METHOD_NAME
                : HttpPost.METHOD_NAME;

        ProxyContext context = getContextFor(contextName);

        BufferedReader reader
                = getReader(method, url, referer, content, context);

        StringBuilder result = new StringBuilder();

        boolean close = minBytes > 0
                ? readBytesFromStream(reader, result, minBytes)
                : readLinesFromStream(reader, result, maxLines);

        if (close || contextName == null) {
            context.close(method, url);
        }

        return result;
    }

    /**
     * read the given input stream all the way to the end and return the result
     * as a string created by interpreting the read bytes as utf-8
     * @param stream
     * @param target
     * @param maxLines
     * @return
     * @throws IOException
     */
    protected boolean readLinesFromStream(BufferedReader stream,
            StringBuilder target, int maxLines) throws IOException {

        String line = null;
        int lineNum = 0;

        while ((maxLines <= 0 || lineNum < maxLines)
                && (line = stream.readLine()) != null) {
            if (lineNum++ > 0) {
                target.append('\n');
            }
            target.append(line);
        }

        return line == null;
    }

    /**
     * Read from the given stream until the given minimum number of bytes is
     * read.
     * @param stream
     * @param target
     * @param minBytes
     * @return
     * @throws IOException
     */
    protected boolean readBytesFromStream(BufferedReader stream,
            StringBuilder target, int minBytes) throws IOException {

        int biteSize = 2048 < minBytes ? 2048 : minBytes;
        char[] buffer = new char[biteSize];
        int bytesRead = -1;
        int totalBytes = 0;

        while ((minBytes <= 0 || totalBytes < minBytes)
                && (bytesRead = stream.read(buffer, 0, biteSize)) >= 0) {
            target.append(buffer, 0, bytesRead);
            totalBytes += bytesRead;
        }

        if (totalBytes < minBytes) {
            throw new IOException("Unexpected end of stream.  Attermped to "
                    + "read " + minBytes + " but only received " + bytesRead);
        }

        return bytesRead < 0;
    }

    /**
     * get the complete or partial contents of the url after performing an http
     * GET to the given url
     * @param url
     * @param referer
     * @param minBytes
     * @param maxLines
     * @param contextName
     * @return
     * @throws IOException
     */
    public StringBuilder get(String url, String referer, int minBytes,
            int maxLines, String contextName) throws IOException {
        return readFromUrl(url, referer, null,
                maxLines, minBytes, contextName);
    }

    /**
     * Perform an http POST to the given url with the given content, and return
     * either the full or requested partial content
     * @param url
     * @param referer
     * @param content
     * @param minBytes
     * @param maxLines
     * @param contextName
     * @return
     * @throws IOException
     */
    public StringBuilder post(String url, String referer, String content,
            int minBytes, int maxLines, String contextName) throws IOException {

        return readFromUrl(url, referer, content,
                maxLines, minBytes, contextName);
    }

    /**
     * reset this http client
     */
    public void reset() {
        for (ProxyContext context : openContexts.values()) {
            context.closeAll();
        }

        openContexts.clear();
    }

    /**
     * Evaluation context for a usable http client connection
     */
    private static class ProxyContext {
        private final HttpClientContext clientContext;
        private final Map<String, CloseableHttpResponse> responses;
        private final Map<String, BufferedReader> readers;

        public ProxyContext(HttpClientContext clientContext) {
            this.clientContext = clientContext;
            responses = new HashMap<String, CloseableHttpResponse>();
            readers = new HashMap<String, BufferedReader>();
        }

        /**
         * @return the clientContext
         */
        public HttpClientContext getClientContext() {
            return clientContext;
        }

        /**
         * get a key for the given method and url
         * @param method
         * @param url
         * @return
         */
        private String getKey(String method, String url) {
            return method + url;
        }

        /**
         * Store the connection in this context
         * @param method
         * @param url
         * @param reader
         * @param response
         */
        public void storeConnection(String method, String url,
                BufferedReader reader, CloseableHttpResponse response) {
            String key = getKey(method, url);

            readers.put(key, reader);
            responses.put(key, response);
        }

        /**
         * Get any connection stored for the given method and url
         * @param method
         * @param url
         * @return
         */
        public BufferedReader getConnection(String method, String url) {
            String key = getKey(method, url);
            return readers.get(key);
        }

        /**
         * Close any open connection to the given url with the given method
         * @param method
         * @param url
         */
        public void close(String method, String url) {
            String key = getKey(method, url);

            CloseableHttpResponse response = responses.remove(key);

            BufferedReader reader = readers.get(key);

            if (reader != null) {
                try {
                    reader.close();
                }
                catch(IOException e) {
                    // Who cares
                }
            }

            if (response != null) {
                try {
                    response.close();
                }
                catch(IOException e) {
                    // Who cares
                }
            }
        }

        /**
         * close all open connections associated with this context
         */
        public void closeAll() {
            for (CloseableHttpResponse response : responses.values()) {
                try {
                    response.close();
                }
                catch(IOException ex) {
                    // Who cares
                }
            }

            for (BufferedReader reader : readers.values()) {
                try {
                    reader.close();
                }
                catch(IOException ex) {
                    // Who cares
                }
            }

            readers.clear();
            responses.clear();
        }
    }
}
