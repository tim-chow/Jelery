package cn.timd.Jelery.ResultBackendImpl;

import cn.timd.Jelery.Exception.ResultBackendException;
import cn.timd.Jelery.ResultBackend;
import cn.timd.Jelery.Vo.TaskResultVo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.util.HashMap;
import java.util.Map;

public class MemoryResultBackend implements ResultBackend {
    private final static Gson gson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create();
    private Map<String, byte[]> map = new HashMap<String, byte[]>();

    public TaskResultVo getResult(String taskID, int timeoutMS) throws ResultBackendException {
        try {
            return gson.fromJson(new String(map.get(taskID)), TaskResultVo.class);
        }catch (JsonSyntaxException ex) {
            ex.printStackTrace();
            return null;
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void setResult(String taskID, TaskResultVo taskResult, int timeoutMS, int expireTimeoutMS)
            throws ResultBackendException {
        if (taskID != null && taskResult != null) {
            map.put(taskID, gson.toJson(taskResult).getBytes());
        }
    }
}
