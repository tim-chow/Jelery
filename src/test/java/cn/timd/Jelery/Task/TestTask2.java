package cn.timd.Jelery.Task;

import cn.timd.Jelery.AbstractTaskBase;
import cn.timd.Jelery.Annotation.Task;
import cn.timd.Jelery.Vo.TaskMessageVo;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

@Task
public class TestTask2 extends AbstractTaskBase {
    public byte[] run(TaskMessageVo taskMessage) throws Throwable {
        throw new NotImplementedException();
    }
}
