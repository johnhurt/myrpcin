package in.myrpc;

/**
 * Special exception for MyRpc errors
 * @author kguthrie
 */
public class MyRpcException extends Exception {

    public MyRpcException() {
    }

    public MyRpcException(String message) {
        super(message);
    }

    public MyRpcException(Throwable cause) {
        super(cause);
    }

    public MyRpcException(String message, Throwable cause) {
        super(message, cause);
    }

}
