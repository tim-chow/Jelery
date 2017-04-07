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
        return 1000000;
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
            "10.125.196.111", 5672, "guest", "guest", "bs-vhost",
            "Jelery", "Jelery", "Jelery");

    private ResultBackend resultBackend = new RedisResultBackend(
            "timd.cn", 6379, 10000, "timchow", 0);

    public MessageBroker getMessageBroker() {
        return messageBroker;
    }

    public ResultBackend getResultBackend() {
        return resultBackend;
    }
}
