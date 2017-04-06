package cn.timd.Jelery;

public interface Configurable {
    String getEncoding();
    int getMessageBrokerReadTimeoutMS();
    int getMessageBrokerSendTimeoutMS();
    int getResultBackendSetTimeoutMS();
    int getSleepTimeoutMS();
    int getResultExpireTimeoutMS();
    int getMaxRetryCount();

    MessageBroker getMessageBroker();
    ResultBackend getResultBackend();
}
