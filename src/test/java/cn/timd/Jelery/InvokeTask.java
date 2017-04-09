package cn.timd.Jelery;

import cn.timd.Jelery.Task.TestTask1;
import cn.timd.Jelery.Task.TestTask2;
import cn.timd.Jelery.Task.TestTask3;
import cn.timd.Jelery.TaskResult.AsyncTaskResult;
import cn.timd.Jelery.Vo.TaskResultVo;

public class InvokeTask {
    public static void main(String[] args) throws Exception {
        Configurable configurable;
        AbstractTaskBase.configure(configurable = new MyConfigurable());
        AbstractTaskBase.taskScan("cn.timd");

        for (int i = 0; i < 100; i++) {
            System.out.println("send task begin");
            AsyncTaskResult asyncTaskResult = new TestTask3().apply("TestTask " + i);
            System.out.println("send task end");
            Thread.sleep(1000);
            System.out.println("result: " + asyncTaskResult.get(111));
        }

        configurable.close();
    }
}
