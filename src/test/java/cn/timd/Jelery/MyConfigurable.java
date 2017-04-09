package cn.timd.Jelery;

import cn.timd.Jelery.MessageBrokerImpl.RabbitMQMessageBroker;
import cn.timd.Jelery.ResultBackendImpl.RedisResultBackend;

public class MyConfigurable implements Configurable {
    public void close() {
        try {
            if (messageBroker != null)
                messageBroker.close();
            if (resultBackend != null)
                resultBackend.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int getResultExpireTimeoutMS() {
        return 10000;
    }

    public String getEncoding() {
        return "UTF-8";
    }

    public int getMessageBrokerReadTimeoutMS() {
        return 30000;
    }

    public int getMessageBrokerSendTimeoutMS() {
        return 10000;
    }

    public int getResultBackendSetTimeoutMS() {
        return 10000;
    }

    public int getSleepTimeoutMS() {
        return 10;
    }

    public int getMaxRetryCount() {
        return 3;
    }

    private MessageBroker messageBroker = new RabbitMQMessageBroker(
            "127.0.0.1", 5672, "guest", "guest", "bs-vhost",
            "Jelery", "Jelery", "Jelery");

    private ResultBackend resultBackend = new RedisResultBackend(
            "127.0.0.1", 6379, getResultBackendSetTimeoutMS(), null, 0);

    public MessageBroker getMessageBroker() {
        return messageBroker;
    }

    public ResultBackend getResultBackend() {
        return resultBackend;
    }
}
