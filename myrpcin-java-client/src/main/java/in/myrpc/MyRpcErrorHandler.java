package in.myrpc;

/**
 * Interface for handling errors from MyRpc
 * @author kguthrie
 */
public interface MyRpcErrorHandler {

    void onError(MyRpcErrorEvent error);

}
