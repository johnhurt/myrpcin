package in.myrpc.receiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Super simple http client class.  This class has some basic methods that allow
 * it to act like a browser and perform get and post operations
 * @author kguthrie
 */
public class HttpHandler {

    private static final String acceptValue =
            "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

    private static final String acceptLanguageValue = "en-US,en;q=0.5";
    private static final String acceptEncodingValue = "gzip, deflate";

    private static final String userAgent = "Mozilla/5.0 (Macintosh; "
            + "Intel Mac OS X 10.9; rv:26.0) Gecko/20100101 Firefox/26.0";

    private static final String postContentType =
            "application/x-www-form-urlencoded; charset=UTF-8";

    private final Map<String, String> cookies;
    private final Map<String, BufferedReader> openStreams;
    private final Map<String, HttpURLConnection> openConnections;

    public HttpHandler() {
        cookies = new HashMap<String, String>();
        openStreams = new HashMap<String, BufferedReader>();
        openConnections = new HashMap<String, HttpURLConnection>();
    }

    /**
     * open a stream to the
     * @param url
     * @param referer
     * @param content
     * @return
     */
    private BufferedReader getStreamForUrl(String url, String referer,
            String content) throws IOException {
        BufferedReader result = openStreams.get(url);

        if (result != null) {
            return result;
        }

        String httpMethod = content != null ? "POST" : "GET";
        HttpURLConnection connection =
                (HttpURLConnection) new URL(url).openConnection();

        // Just in case set user agent to something unobjectionable
        connection.addRequestProperty("Accept", acceptValue);
        connection.addRequestProperty("User-Agent", userAgent);
        connection.addRequestProperty("Accept-Encoding",
                acceptEncodingValue);

        if (!cookies.isEmpty()) {
            StringBuilder cookieString = new StringBuilder();

            int i = 0;

            for (Map.Entry cookieEntry : cookies.entrySet()) {
                if (i++ > 0) {
                    cookieString.append("; ");
                }

                cookieString.append(cookieEntry.getKey());
                cookieString.append("=");
                cookieString.append(cookieEntry.getValue());
            }

            connection.addRequestProperty("Cookie",
                    cookieString.toString());
        }

        connection.addRequestProperty("Referer", referer);

        connection.setRequestMethod(httpMethod);
        connection.setRequestProperty("Host",
                connection.getURL().getHost());
        connection.setRequestProperty("Accept-Language",
                acceptLanguageValue);

        if (httpMethod.equals("POST")) {
            connection.setDoOutput(true);
            connection.addRequestProperty("Content-Type", postContentType);
            connection.addRequestProperty("Cache-Control", "no-cache");
            connection.addRequestProperty("Pragma", "no-cache");

        }

        if (content != null) {
            byte[] contentBytes = content.getBytes();

            connection.addRequestProperty("Content-Length",
                    "" + contentBytes.length);

            OutputStream wr = connection.getOutputStream();
            wr.write(contentBytes);
            wr.flush();
            wr.close();
        }

        String setCookie = connection.getHeaderField("Set-Cookie");

        if (setCookie != null) {
            String name = setCookie.split("=", 2)[0];
            String value = setCookie.split(";", 2)[0];
            value = value.substring(name.length() + 1);
            cookies.put(name, value);
        }

        String contentEncoding =
                connection.getHeaderField("Content-Encoding");

        try {
            result = new BufferedReader( new InputStreamReader(
                    (contentEncoding != null
                            && contentEncoding.contains("gzip"))
                    ? new GZIPInputStream(connection.getInputStream())
                    : connection.getInputStream()));
        }
        catch (IOException ex) {
            BufferedReader br = new BufferedReader( new InputStreamReader(
                    (contentEncoding != null
                            && contentEncoding.contains("gzip"))
                    ? new GZIPInputStream(connection.getErrorStream())
                    : connection.getErrorStream()));

            StringBuilder error = new StringBuilder();

            readLinesFromStream(br, error, 0);

            System.err.println("Http Error: \n\n"
                    + error.toString() + "\n\n");

            throw ex;
        }

        openConnections.put(url, connection);
        openStreams.put(url, result);

        return result;
    }

    /**
     * perform a get or post operation (depending on the presence of content)
     * and return all or a portion of the response based on the given minBytes
     * and maxLines.
     * @param url http url to read from
     * @param referer refering url
     * @param content post content if any
     * @param minBytes minimum number of bytes to read or zero if no byte limit
     * @param maxLines maximum number of lines to read or zero if no limit
     * @return
     * @throws IOException
     */
    private StringBuilder performHttpOperation(final String url, String referer,
            String content, int minBytes, int maxLines) throws IOException {

        BufferedReader reader = getStreamForUrl(url, referer, content);

        StringBuilder result = new StringBuilder();

        boolean close = readWithTimeout(reader, minBytes,
                maxLines, result, 45000);

        if (close) {
            close(url);
        }

        return result;
    }

    /**
     * Read the configured amount from the given reader, but throw an IO timeout
     * exception if the configured amount is not consumed before the given
     * interval expires
     * @param stream
     * @param timeoutInMillis
     * @param minBytes
     * @param result
     * @param maxLines
     * @return
     * @throws java.io.IOException
     */
    protected boolean readWithTimeout(final BufferedReader stream,
            final int minBytes, final int maxLines, final StringBuilder result,
            long timeoutInMillis) throws IOException {

        final boolean[] closePointer = { true };
        final boolean[] donePointer = { false };
        final Throwable[] exceptionResultPointer = { null };

        // Because buffered readers on sockets cannot be easily interrupted
        // or closed, we run the read operation in a different thread and
        // monitor the timeout interval in the current thread.
        new Thread(new Runnable() {

            public void run() {

                try {
                    closePointer[0] = minBytes > 0
                            ? readBytesFromStream(stream, result, minBytes)
                            : readLinesFromStream(stream, result, maxLines);
                }
                catch (IOException ex) {
                    exceptionResultPointer[0] = ex;
                }
                catch (RuntimeException rex) {
                    exceptionResultPointer[0] = rex;
                }

                donePointer[0] = true;

                synchronized (HttpHandler.this) {
                    HttpHandler.this.notifyAll();
                }
            }

        }).start();

        long now;
        long stop = System.currentTimeMillis() + timeoutInMillis;

        synchronized (this) {
            while (!donePointer[0]
                    && (now = System.currentTimeMillis()) < stop) {
                try {

                    long waitInterval = (stop - now) / 2;
                    waitInterval = 1000 < waitInterval ? waitInterval : 1000;

                    this.wait(waitInterval);
                }
                catch (InterruptedException ie) {
                    break;
                }
            }
        }

        if (!donePointer[0]) {
            throw new IOException("Read timeout");
        }
        if (exceptionResultPointer[0] != null) {
            throw new IOException("Read failed", exceptionResultPointer[0]);
        }

        return closePointer[0];
    }

    /**
     * Closes any connections or readers open to the given url
     * @param url
     */
    protected void close(String url) {
        BufferedReader reader = openStreams.remove(url);

        if (reader != null) {
            try {
                reader.close();
            }
            catch(IOException ex) {
                // disregaurd
            }
        }

        HttpURLConnection connection = openConnections.remove(url);

        if (connection != null) {
            connection.disconnect();
        }
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
     * @return
     * @throws IOException
     */
    public StringBuilder get(String url, String referer, int minBytes,
            int maxLines) throws IOException {
        return performHttpOperation(url, referer, null, minBytes, maxLines);
    }

    /**
     * Perform an http POST to the given url with the given content, and return
     * either the full or requested partial content
     * @param url
     * @param referer
     * @param content
     * @param minBytes
     * @param maxLines
     * @return
     * @throws IOException
     */
    public StringBuilder post(final String url, final String referer,
            final String content, final int minBytes, final int maxLines)
                    throws IOException {


        return performHttpOperation(url, referer, content, minBytes, maxLines);
    }

    /**
     * reset this http client
     */
    public void reset() {
        for (final BufferedReader con : openStreams.values()) {
            // This close call can apparently hang, so perform it on a separate
            // thread
            new Thread(new Runnable() {

                public void run() {
                    try {
                        con.close();
                    }
                    catch(IOException ex) {
                        //Disregard
                    }
                }
            }).start();
        }

        for (HttpURLConnection connection : openConnections.values()) {
            connection.disconnect();
        }

        openStreams.clear();
        cookies.clear();
    }
}
