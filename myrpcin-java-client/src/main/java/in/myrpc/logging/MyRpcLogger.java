package in.myrpc.logging;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulation of the logic for logging messages into zero or many logging
 * callbacks
 * @author kguthrie
 */
public class MyRpcLogger {

    public static String throwableToString(Throwable t) {
        StringBuilder errorMessage = new StringBuilder();

        Throwable cause = t;
        String spacer = "  - ";

        while (cause != null) {
            errorMessage.append(cause.getClass().getName());
            errorMessage.append(": ");
            errorMessage.append(cause.getMessage());
            errorMessage.append("\n");

            for (StackTraceElement e : cause.getStackTrace()) {
                errorMessage.append(spacer);
                errorMessage.append(e.toString());
                errorMessage.append("\n");
            }

            cause = cause.getCause();

            if (cause != null) {
                errorMessage.append("\nCaused By:\n");
            }
        }

        return errorMessage.toString();
    }

    private final List<MyRpcLoggingCallback> callbacks;

    public MyRpcLogger() {
        this.callbacks = new ArrayList<MyRpcLoggingCallback>();
    }

    /**
     * Add the given callback
     * @param callback
     */
    public void addCallback(MyRpcLoggingCallback callback) {
        this.callbacks.add(callback);
    }

    /**
     * Remove the given callback and return whether or not the callback was
     * there to begin with
     * @param callback
     * @return
     */
    public boolean removeCallback(MyRpcLoggingCallback callback) {
        return callbacks.remove(callback);
    }

    /**
     * Write the given log message with the given level to all of the
     * logging callbacks
     * @param level
     * @param message
     */
    public void logMessage(MyRpcLoggingLevel level, String message) {
        for (MyRpcLoggingCallback callback : callbacks) {
            callback.onLog(level, message);
        }
    }

    /**
     * Write the given message and exception to the loggers with the given level
     * @param level
     * @param message
     * @param t
     */
    public void logMessage(MyRpcLoggingLevel level, String message,
            Throwable t) {
        String throwableAsString = throwableToString(t);
        MyRpcLogger.this.logMessage(level, message
                + "\n\n" + throwableAsString);
    }

    public void trace(String message) {
        MyRpcLogger.this.logMessage(MyRpcLoggingLevel.trace, message);
    }

    public void debug(String message) {
        MyRpcLogger.this.logMessage(MyRpcLoggingLevel.debug, message);
    }

    public void info(String message) {
        MyRpcLogger.this.logMessage(MyRpcLoggingLevel.info, message);
    }

    public void warn(String message) {
        MyRpcLogger.this.logMessage(MyRpcLoggingLevel.warn, message);
    }

    public void error(String message) {
        MyRpcLogger.this.logMessage(MyRpcLoggingLevel.error, message);
    }

    public void fatal(String message) {
        MyRpcLogger.this.logMessage(MyRpcLoggingLevel.fatal, message);
    }

    public void trace(String message, Throwable t) {
        logMessage(MyRpcLoggingLevel.trace, message, t);
    }

    public void debug(String message, Throwable t) {
        logMessage(MyRpcLoggingLevel.debug, message, t);
    }

    public void info(String message, Throwable t) {
        logMessage(MyRpcLoggingLevel.info, message, t);
    }

    public void warn(String message, Throwable t) {
        logMessage(MyRpcLoggingLevel.warn, message, t);
    }

    public void error(String message, Throwable t) {
        logMessage(MyRpcLoggingLevel.error, message, t);
    }

    public void fatal(String message, Throwable t) {
        logMessage(MyRpcLoggingLevel.fatal, message, t);
    }
}
