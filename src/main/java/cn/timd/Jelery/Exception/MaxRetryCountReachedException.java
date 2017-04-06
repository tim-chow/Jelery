package cn.timd.Jelery.Exception;

public class MaxRetryCountReachedException extends JeleryException {
    public MaxRetryCountReachedException(String message) {
        super(message);
    }
}
