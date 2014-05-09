package in.myrpc.receiver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import in.myrpc.MyRpcException;
import in.myrpc.receiver.model.AppEngineChannelConnectPayload;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Message receiver the "listens" on an appengine channel.  Really this does
 * a long poll to
 * @author kguthrie
 */
public class AppEngineChannelMessageReceiver extends MessageReceiver {

    private static final String myRpcUrl = "https://www.myrpc.in";

    private static final String talkGadgetUrlFormat
            = "https://%s.talkgadget.google.com/talkgadget";

    private static final String talkGadgetBlankUrlFormat
            = "%s/xpc_blank";

    private static final String myRpcBlankChannelUrl
            = myRpcUrl + "/_ah/channel/xpc_blank";

    private static final String connectUrlFormat
            = "%s/d?token=%s&xpc=%s";

    private static final String acceptValue =
            "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

    private static final String acceptLanguageValue = "en-US,en;q=0.5";
    private static final String acceptEncodingValue = "gzip, deflate";

    private static final String userAgent = "Mozilla/5.0 (Macintosh; "
            + "Intel Mac OS X 10.9; rv:26.0) Gecko/20100101 Firefox/26.0";

    private static final String talkgadgetTest1UrlFormat =
            "%s/dch/test?VER=8&clid=%s&gsessionid=%s&prop=data&token=%s"
            + "&ec=%%5B%%22ci%%3Aec%%22%%5D&MODE=init&zx=%s&t=1";

    private static final String talkgadgetTest2UrlFormat =
            "%s/dch/test?VER=8&clid=%s&gsessionid=%s&prop=data&token=%s"
            + "&ec=%%5B%%22ci%%3Aec%%22%%5D&TYPE=xmlhttp&zx=%s&t=1";

    private static final Pattern importantStringPattern =
            Pattern.compile("\\s\\\"([a-zA-Z0-9\\_\\-]{5,})\\\"\\,");

    private static final Pattern sidSelectorFromBindResult =
            Pattern.compile("\\\"([a-zA-Z0-9\\_\\-]{3,})\\\"");

    private static final String talkgadgetBindUrlFormat =
            "%s/dch/bind?VER=8&clid=%s&gsessionid=%s&prop=data&token=%s"
            + "&ec=%%5B%%22ci%%3Aec%%22%%5D&RID=%d&CVER=1&zx=%s&t=1";

    private static final String talkgadgetConnectUrlFormat =
            "%s/dch/bind?VER=8&clid=%s&gsessionid=%s&prop=data&token=%s"
            + "&ec=%%5B%%22ci%%3Aec%%22%%5D&SID=%s&RID=%d&AID=2&zx=%s&t=1";

    private static final String talkgadgetConnectAddPayloadFormat =
            "count=1&ofs=0&req0_m=%%5B%%22connect-add-client%%22%%5D&req0_c=%s"
            + "&req0__sc=c";

    private static final String talkGadgetLongPollUrlFormat =
            "%s/dch/bind?VER=8&clid=%s&gsessionid=%s&prop=data&token=%s"
            + "&ec=%%5B%%22ci%%3Aec%%22%%5D&RID=rpc&SID=%s&CI=0&AID=%s"
            + "&TYPE=xmlhttp&zx=%s&t=1";

    private static final String postContentType =
            "application/x-www-form-urlencoded; charset=UTF-8";

    private static final Pattern connectionNumberPattern = Pattern.compile(
            "\\[\\\"p\\\".*?\\@appspot\\.com.*?(\\d{5,})\\]",
            Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);

    private final Random random;

    private final String talkGadgetUrl;
    private final String talkGadgetBlankUrl;
    private final ObjectMapper mapper;
    private String clid;
    private String gsessionid;
    private String sid;
    private final AtomicInteger rid;
    private final AtomicInteger aid;

    private Poller poller;
    private Thread pollerThread;

    public AppEngineChannelMessageReceiver(String token,
            MessageHandler handler) {
        super(token, handler);

        mapper = new ObjectMapper();
        random = new Random(System.currentTimeMillis());
        String randomNumber = createRandomNumberString(3);

        talkGadgetUrl = String.format(talkGadgetUrlFormat, randomNumber);
        talkGadgetBlankUrl
                = String.format(talkGadgetBlankUrlFormat, talkGadgetUrl);
        rid = new AtomicInteger(random.nextInt(99999));
        aid = new AtomicInteger(random.nextInt(9) + 1);
    }

    /**
     * create a random string of the given length
     * @param length
     * @return
     */
    private String createRandomString(int length) {
        char[] result = new char[length];

        for (int i = 0; i < length; i++) {
            int rand = random.nextInt(26 * 2 + 10);

            if (rand < 10) {
                result[i] = (char)('0' + rand);
            }
            else if (rand < 36) {
                result[i] = (char)('a' + (rand - 10));
            }
            else {
                result[i] = (char)('A' + (rand - 36));
            }
        }

        return new String(result);
    }

    /**
     * Create a strong of just number with the given length
     * @param length
     * @return
     */
    private String createRandomNumberString(int length) {
        char[] result = new char[length];

        for (int i = 0; i < length; i++) {
            int rand = random.nextInt(10);

            result[i] = (char)('0' + rand);
        }

        return new String(result);
    }

    /**
     * read the given input stream all the way to the end and return the result
     * as a string created by interpreting the read bytes as utf-8
     * @param iStream
     * @return
     * @throws IOException
     */
    private String readStreamToEnd(InputStream iStream, int max)
            throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int bytesRead;
        int totalBytesRead = 0;

        while ((bytesRead = iStream.read(buffer)) > 0) {
            result.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
            if (max > 0 && totalBytesRead >= max) {
                break;
            }
        }

        return new String(result.toByteArray(), "utf-8");
    }

    /**
     * Extract the important strings from the given content using the pattern
     * defined in the variable importantStringPattern
     * @param content
     * @return
     */
    private String[] getImportantStringsFromContent(String content) {
        Matcher matcher = importantStringPattern.matcher(content);
        List<String> result = Lists.newArrayList();

        while (matcher.find()) {
            String importantString = matcher.group(1);
            if (token.equals(importantString)) {
                continue;
            }
            result.add(importantString);
        }

        return result.toArray(new String[]{});
    }

    /**
     * Create and http connection for a request that has no content (get)
     * @param url
     * @param referer
     * @param cookies
     * @return
     * @throws IOException
     */
    private HttpURLConnection createHttpConnection(String url, String referer,
            Map<String, String> cookies) throws IOException {
        return createHttpConnection(url, referer, cookies, null);
    }

    /**
     * Create a http connection with the headers and properties already set
     * @param getOrPost
     * @param url
     * @param referer
     * @param cookies
     * @return
     */
    private HttpURLConnection createHttpConnection(String url, String referer,
            Map<String, String> cookies, String content) throws IOException {

        assert(cookies != null);

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

        connection.addRequestProperty("Referer",
                referer == null ? myRpcUrl : referer);

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

            if (content != null) {

                byte[] contentBytes = content.getBytes();

                connection.addRequestProperty("Content-Length",
                        "" + contentBytes.length);

                OutputStream wr = connection.getOutputStream();
                wr.write(contentBytes);
                wr.flush();
                wr.close();
            }
        }


        String setCookie = connection.getHeaderField("Set-Cookie");

        if (setCookie != null) {
            String name = setCookie.split("=", 2)[0];
            String value = setCookie.split(";", 2)[0];
            value = value.substring(name.length() + 1);
            cookies.put(name, value);
        }


        return connection;
    }

    /**
     * get the complete contents of the url after performing an http GET
     * @param url
     * @param referer
     * @param cookies
     * @param maxBytes
     * @return
     * @throws IOException
     */
    public String get(String url, String referer, Map<String, String> cookies,
            int maxBytes) throws IOException {
        HttpURLConnection connection = null;
        assert(cookies != null);

        try {
            connection = createHttpConnection(url, referer, cookies);

            String contentEncoding =
                    connection.getHeaderField("Content-Encoding");

            String result = readStreamToEnd( (contentEncoding != null
                    && contentEncoding.contains("gzip"))
                            ? new GZIPInputStream(connection.getInputStream())
                            : connection.getInputStream()
                   , maxBytes);

            return result;
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public String post(String url, String referer,
            Map<String, String> cookies, String content) throws IOException {
        HttpURLConnection connection = null;
        assert(cookies != null);

        try {
            connection = createHttpConnection(url, referer, cookies, content);

            String contentEncoding =
                    connection.getHeaderField("Content-Encoding");

            String result = readStreamToEnd( (contentEncoding != null
                    && contentEncoding.contains("gzip"))
                            ? new GZIPInputStream(connection.getInputStream())
                            : connection.getInputStream()
                   , 0);

            return result;
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Open the connection to myrpc's appengine channel api
     * @throws MyRpcException
     */
    public void open() throws MyRpcException {

        String randomString = createRandomString(10);

        AppEngineChannelConnectPayload connectPayload =
                new AppEngineChannelConnectPayload(randomString, null,
                        talkGadgetBlankUrl,
                        myRpcBlankChannelUrl);

        String encondedConnectPayload;

        try {
            encondedConnectPayload = URLEncoder.encode(
                    mapper.writeValueAsString(connectPayload), "utf-8");
        }
        catch (IOException e) {
            throw new MyRpcException(e);
        }

        String connectUrl = String.format(connectUrlFormat,
                talkGadgetUrl, this.token, encondedConnectPayload);

        try {
            Map<String, String> cookies = Maps.newHashMap();

            String connectResult = get(connectUrl, null, cookies, 0);
            String[] importantStrings = getImportantStringsFromContent(
                    connectResult);

            connectResult = null;

            clid = importantStrings[0];
            gsessionid = importantStrings[1];
            String newRandomString = createRandomString(12).toLowerCase();

            String testUrl = String.format(talkgadgetTest1UrlFormat,
                    talkGadgetUrl, clid, gsessionid, token, newRandomString);

            String testResult = get(testUrl, connectUrl, cookies, 2);
            //System.out.println("Test result 1: " + testResult);
            newRandomString = createRandomString(12).toLowerCase();

            testUrl = String.format(talkgadgetTest2UrlFormat,
                    talkGadgetUrl, clid, gsessionid, token, newRandomString);

            testResult = get(testUrl, connectUrl, cookies, 5);

            //System.out.println("Test result 2: " + testResult);

            String bindUrlForPost = String.format(talkgadgetBindUrlFormat,
                    talkGadgetUrl, clid, gsessionid, token,
                    rid.incrementAndGet(),
                    createRandomString(12).toLowerCase());

            String bindResult = post(bindUrlForPost, connectUrl, cookies,
                    "count=0");

            sid = parseSidFromBindResult(bindResult);

            //System.out.println("Got sid = " + sid);

            String connectAddUrl = String.format(talkgadgetConnectUrlFormat,
                    talkGadgetUrl, clid, gsessionid, token, sid,
                    rid.incrementAndGet(),
                    createRandomString(12).toLowerCase());


            poller = new Poller(connectUrl, cookies);

            pollerThread = new Thread(poller, "poller");
            pollerThread.start();

            String connectAddContent = String.format(
                    talkgadgetConnectAddPayloadFormat, clid);

            String connectAddResult = post(connectAddUrl, connectUrl,
                    cookies, connectAddContent);

            //System.out.println(connectAddResult);

        }
        catch(IOException ex) {
            throw new MyRpcException("Failed to open connection to appengine "
                    + "channel", ex);
        }

    }

    /**
     * get the sid from the result of the first bind call
     * @param bindResult
     * @return
     */
    public String parseSidFromBindResult(String bindResult) {
        Matcher matcher = sidSelectorFromBindResult.matcher(bindResult);

        while (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }


    public void close() throws MyRpcException {
        poller.close();
    }

    /**
     * Private class that represents the actual message listener.  The thread
     * created for this class will block on long poll after long poll and
     * relay translated messages to the actual listener
     */
    private class Poller implements Runnable {

        private boolean keepGoing;
        private final String referer;
        private final Map<String, String> cookies;

        private HttpURLConnection connection;
        private BufferedReader pollReader;
        private long conntectionNumber;
        private String connectionSegment;


        public Poller(String referer, Map<String, String> cookies) {
            keepGoing = true;
            this.referer = referer;
            this.cookies = cookies;
        }

        @Override
        public void run() {

            connection = null;
            pollReader = null;
            int messageCount = 0;

            while (keepGoing) {
                try {
                    if (connection == null || pollReader == null) {
                        connection = getPollConnection();
                        pollReader = openStream(connection);
                        messageCount = 0;
                    }

                    String result = pollReader.readLine();

                    if (result == null) { // End of stream
                        pollReader.close();
                        connection.disconnect();
                        connection = null;
                        continue;
                    }

                    int length;

                    try {
                        length = Integer.parseInt(result);
                    }
                    catch (NumberFormatException nfe) {
                        // The first line in the message should be a number.  If
                        // not, then this is an error.
                        if (keepGoing) {
                            handler.onError(2000, nfe.getMessage());
                        }
                        break;
                    }

                    String message = readAndParseMessage(length, pollReader,
                            messageCount++);

                    System.out.println(message);
                }
                catch (IOException e) {
                    if (keepGoing) {
                        handler.onError(1000, e.getMessage());
                    }
                    break;
                }
            }

            close();
        }

        /**
         * Read and parse a message of the given length from the buffer
         * @param length
         * @param reader
         * @return
         */
        private String readAndParseMessage(int length, BufferedReader reader,
                int messageNumber) throws IOException {
            StringBuilder rawMessage = new StringBuilder(length);

            String line;
            int charCount = 0;

            while (true) {
                line = reader.readLine();

                // If the stream ends before we get the correct number of
                // characters then this is an error
                if (line == null) {
                    return null;
                }

                charCount += line.length() + 1;
                rawMessage.append(line);
                rawMessage.append("\n");

                if (charCount >= length) {
                    break;
                }
            }

            if (messageNumber == 0) {
                Matcher matcher = connectionNumberPattern.matcher(rawMessage);
                if (matcher.find()) {
                    conntectionNumber = Long.parseLong(matcher.group(1));
                }

                return null;
            }

            return null;
        }

        public void close() {

            keepGoing = false;

            if (connection != null) {
                connection.disconnect();
            }

            if (pollReader != null) {
                try {
                    pollReader.close();
                }
                catch (IOException ex) {
                    // Who cares
                }
            }
        }

        /**
         * Get the long poll connection
         * @return
         */
        private HttpURLConnection getPollConnection() throws IOException {
            String url = String.format(talkGadgetLongPollUrlFormat,
                                talkGadgetUrl, clid, gsessionid, token, sid,
                                        aid.incrementAndGet(),
                                        createRandomString(12).toLowerCase());

            HttpURLConnection result = createHttpConnection(url,
                    referer, cookies);

            return result;
        }

        private BufferedReader openStream(HttpURLConnection connection)
                throws IOException {

            return new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
        }
    }

}
