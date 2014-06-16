package in.myrpc.logging;

/**
 * Callback to tie MyRpc logging messages into the logging for the host
 * application
 * @author kguthrie
 */
public interface MyRpcLoggingCallback {

    void onLog(MyRpcLoggingLevel level, String message);

}
