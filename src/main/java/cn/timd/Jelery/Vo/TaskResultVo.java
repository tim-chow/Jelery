package cn.timd.Jelery.Vo;

import java.util.Arrays;

public class TaskResultVo {
    private String state;
    private String exceptionClassName;
    private String exceptionMessage;
    private byte[] result;
    private String encoding;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getExceptionClassName() {
        return exceptionClassName;
    }

    public void setExceptionClassName(String exceptionClassName) {
        this.exceptionClassName = exceptionClassName;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public byte[] getResult() {
        return result;
    }

    public void setResult(byte[] result) {
        this.result = result;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Override
    public String toString() {
        return "TaskResultVo{" +
                "state='" + state + '\'' +
                ", exceptionClassName='" + exceptionClassName + '\'' +
                ", exceptionMessage='" + exceptionMessage + '\'' +
                ", result=" + Arrays.toString(result) +
                ", encoding='" + encoding + '\'' +
                '}';
    }
}
