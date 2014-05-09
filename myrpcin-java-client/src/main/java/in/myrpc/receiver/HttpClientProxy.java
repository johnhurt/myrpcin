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
public class HttpClientProxy {

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

    public HttpClientProxy() {
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
            Thread.sleep(150);
        }
        catch (Exception ex) {

        }

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
    private StringBuilder performHttpOperation(String url, String referer,
            String content, int minBytes, int maxLines)
                    throws IOException {

        BufferedReader reader = getStreamForUrl(url, referer, content);

        StringBuilder result = new StringBuilder();

        boolean close = minBytes > 0
                ? readBytesFromStream(reader, result, minBytes)
                : readLinesFromStream(reader, result, maxLines);

        if (close) {
            try {
                reader.close();
            }
            catch(IOException ex) {
                // disregaurd
            }
            openStreams.remove(url);
            HttpURLConnection connection = openConnections.remove(url);
            if (connection != null) {
                connection.disconnect();
            }
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
        for (BufferedReader con : openStreams.values()) {
            try {
                con.close();
            }
            catch(IOException ex) {
                //Disregard
            }
        }

        for (HttpURLConnection connection : openConnections.values()) {
            connection.disconnect();
        }

        openStreams.clear();
        cookies.clear();
    }
}
