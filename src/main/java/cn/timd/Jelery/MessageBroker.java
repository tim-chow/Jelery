package cn.timd.Jelery;

import cn.timd.Jelery.Exception.MessageBrokerException;
import cn.timd.Jelery.Vo.TaskMessageVo;

public interface MessageBroker {
    void sendMessage(TaskMessageVo taskMessage, int timeoutMS) throws MessageBrokerException;
    TaskMessageVo getMessage(int timeoutMS) throws MessageBrokerException;
}
