package in.myrpc;

/**
 * Represents the event of an error from MyRpc, and allows for an automatic
 * reconnect to be canceled.
 * @author kguthrie
 */
public class MyRpcErrorEvent {

    private final String message;

    private boolean reconnect;

    public MyRpcErrorEvent(String message) {
        this.message = message;
        reconnect = true;
    }

    public void disableReconnect() {
        reconnect = false;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the reconnect
     */
    public boolean doReconnect() {
        return reconnect;
    }

}
