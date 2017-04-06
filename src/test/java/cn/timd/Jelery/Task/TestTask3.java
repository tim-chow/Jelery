package cn.timd.Jelery.Task;

import cn.timd.Jelery.AbstractTaskBase;
import cn.timd.Jelery.Annotation.Task;
import cn.timd.Jelery.Exception.RetryException;
import cn.timd.Jelery.Vo.TaskMessageVo;

@Task
public class TestTask3 extends AbstractTaskBase {
    public byte[] run(TaskMessageVo taskMessage) throws Throwable {
        System.out.println("count: " + taskMessage.getRetryCount());
        if (taskMessage.getRetryCount() < 1)
            throw new RetryException("retry");
        return null;
    }
}
