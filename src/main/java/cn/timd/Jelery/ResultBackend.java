package cn.timd.Jelery;

import cn.timd.Jelery.Exception.ResultBackendException;
import cn.timd.Jelery.Vo.TaskResultVo;

public interface ResultBackend {
    void setResult(String taskID, TaskResultVo taskResult, int timeoutMS, int expireTimeoutMS)
            throws ResultBackendException;
    TaskResultVo getResult(String taskID, int timeoutMS) throws ResultBackendException;
}
