package cn.timd.Jelery;

import cn.timd.Jelery.Exception.MessageBrokerException;
import cn.timd.Jelery.Exception.ResultBackendException;
import cn.timd.Jelery.MessageBrokerImpl.MemoryMessageBroker;
import cn.timd.Jelery.ResultBackendImpl.MemoryResultBackend;
import cn.timd.Jelery.Task.TestTask1;
import cn.timd.Jelery.Task.TestTask2;
import cn.timd.Jelery.TaskResult.AsyncTaskResult;
import cn.timd.Jelery.Vo.TaskResultVo;

import java.io.UnsupportedEncodingException;

class MyConfigurable implements Configurable {
    public  int getResultExpireTimeoutMS() {return 100000000;}
    public String getEncoding() {
        return "UTF-8";
    }
    public int getMessageBrokerReadTimeoutMS() {
        return 3000;
    }
    public int getMessageBrokerSendTimeoutMS() {
        return 3000;
    }
    public int getResultBackendSetTimeoutMS() {
        return 3000;
    }
    public int getSleepTimeoutMS() {
        return 1000;
    }

    private MessageBroker messageBroker = new MemoryMessageBroker();
    private ResultBackend resultBackend = new MemoryResultBackend();

    public MessageBroker getMessageBroker() {
        return messageBroker;
    }
    public ResultBackend getResultBackend() {
        return resultBackend;
    }
}

public class Test {
    public static void main(String[] args) throws MessageBrokerException,
            UnsupportedEncodingException, ResultBackendException {
        AbstractTaskBase.configure(new MyConfigurable());
        AbstractTaskBase.taskScan("cn.timd");

        TestTask1 task1 = new TestTask1();
        AsyncTaskResult asyncTaskResult1 = task1.apply("data1");
        TestTask2 task2 = new TestTask2();
        AsyncTaskResult asyncTaskResult2 = task2.apply("data2");
        AbstractTaskBase.run();

        TaskResultVo taskResultVo;
        System.out.println(taskResultVo = asyncTaskResult1.get(111));
        if (taskResultVo != null) {
            System.out.println(taskResultVo.getState());
            System.out.println(new String(taskResultVo.getResult(),
                    taskResultVo.getEncoding()));
        }

        System.out.println(taskResultVo = asyncTaskResult2.get(222));
        if (taskResultVo != null) {
            System.out.println(taskResultVo.getState());
            System.out.println(taskResultVo.getExceptionClassName());
            System.out.println(taskResultVo.getExceptionMessage());
        }
    }
}
