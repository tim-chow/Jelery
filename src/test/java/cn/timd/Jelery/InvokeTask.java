package cn.timd.Jelery;

import cn.timd.Jelery.Task.TestTask1;
import cn.timd.Jelery.Task.TestTask2;
import cn.timd.Jelery.Task.TestTask3;
import cn.timd.Jelery.TaskResult.AsyncTaskResult;

public class InvokeTask {
    public static void main(String[] args) throws Exception {
        Configurable configurable;
        AbstractTaskBase.configure(configurable = new MyConfigurable());
        AbstractTaskBase.taskScan("cn.timd");

        AsyncTaskResult asyncTaskResult = new TestTask3().apply("TestTask1");
        System.out.println(asyncTaskResult.get(1000));
        configurable.close();
    }
}
