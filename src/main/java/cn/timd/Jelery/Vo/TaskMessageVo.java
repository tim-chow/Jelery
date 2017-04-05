package cn.timd.Jelery.Vo;

import java.util.Arrays;

public class TaskMessageVo {
    private String taskID;
    private String taskName;
    private byte[] data;
    private String encoding;

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Override
    public String toString() {
        return "TaskMessageVo{" +
                "taskID='" + taskID + '\'' +
                ", taskName='" + taskName + '\'' +
                ", data=" + Arrays.toString(data) +
                ", encoding='" + encoding + '\'' +
                '}';
    }
}
