package in.myrpc;

import in.myrpc.logging.MyRpcLoggingCallback;
import in.myrpc.logging.MyRpcLoggingLevel;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.HashMap;

public class App implements Runnable, MyRpcErrorHandler {

    private static final String configFile = "config.json";

    public static void main(String[] args) throws Exception {
        Config config = new Config(new File(configFile));
        App app = new App(config);
        new Thread(app, "main").start();
    }

    private final MyRpc myRpc;
    private final Config config;

    public App(Config config) throws IOException, MyRpcException {
        myRpc = new MyRpc(config.getEndpointLocator(),
                new RpcMethods(), this, false);
        this.config = config;

        if (config.getEndpointLocator() == null) {
            String endpointLocator = myRpc.provision(
                    InetAddress.getLocalHost().getHostName(),
                    config.getCenterpointLocator());
            config.setEndpointLocator(endpointLocator);
            config.save();
        }

        myRpc.addLoggingCallback(new MyRpcLoggingCallback() {

            public void onLog(MyRpcLoggingLevel level, String message) {
                System.out.println(level.toString() + " " + message + "\n");
            }
        });

        myRpc.start();
    }

    /**
     * Run the app
     */
    public void run() {
        String message;

        try {
            System.out.println("Who would you like to message?  "
                    + "Press enter to just talk to yourself:");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(System.in));

            String target = in.readLine();

            if (target == null || target.trim().length() == 0) {
                target = config.getEndpointLocator();
            }

            System.out.println("Great, start sending:");

            while ((message = in.readLine()) != null) {
                final long start = System.currentTimeMillis();
                final String finalMessage = message;
                myRpc.call(target, "onMessage",
                        new HashMap<String, String>()
                                {{put("message", finalMessage);}},
                        new MyRpcCallback() {

                            public void onSuccss(String result) {
                                long stop = System.currentTimeMillis();
                                long mid = Long.valueOf(result);
                                System.out.println("One-way time: "
                                        + (mid - start) + "ms");
                                System.out.println("Round-trip time: "
                                        + (stop - start) + "ms");
                            }
                        });
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onError(MyRpcErrorEvent error) {
        System.out.println("Error: " + error.getMessage());
    }

}
