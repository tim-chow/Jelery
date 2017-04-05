package cn.timd.Jelery;

import cn.timd.Jelery.Vo.TaskMessageVo;

public interface TaskBase {
    byte[] run(TaskMessageVo taskMessageVo) throws Throwable;
}
