package cn.timd.Jelery.TaskResult;

import cn.timd.Jelery.Exception.ResultBackendException;
import cn.timd.Jelery.Exception.ResultFormatException;
import cn.timd.Jelery.ResultBackend;
import cn.timd.Jelery.Vo.TaskResultVo;

public class AsyncTaskResult {
    private String taskID;
    private ResultBackend resultBackend;

    public AsyncTaskResult(String taskID, ResultBackend resultBackend) {
        this.taskID = taskID;
        this.resultBackend = resultBackend;
    }

    public String getTaskID() {
        return taskID;
    }

    public TaskResultVo get(int timeoutMS) throws ResultBackendException, ResultFormatException {
        return resultBackend.getResult(taskID, timeoutMS);
    }
}
