package in.myrpc.receiver;

/**
 * Interface methods for handling messages received from messages receiver
 *
 * @author kguthrie
 */
public interface MessageHandler {

    void onOpen();

    void onClose();

    void onMessage(String message);

    void onError(String message);

}
