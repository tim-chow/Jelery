package cn.timd.Jelery;

import cn.timd.Jelery.Exception.MessageBrokerException;
import cn.timd.Jelery.Exception.ResultBackendException;
import java.io.UnsupportedEncodingException;

public class Worker {
    public static void main(String[] args) throws MessageBrokerException,
            UnsupportedEncodingException, ResultBackendException {
        AbstractTaskBase.configure(new MyConfigurable());
        AbstractTaskBase.taskScan("cn.timd");
        AbstractTaskBase.run();
    }
}
