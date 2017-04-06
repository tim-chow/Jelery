package cn.timd.Jelery.MessageBrokerImpl;

import cn.timd.Jelery.Exception.MessageBrokerException;
import cn.timd.Jelery.Exception.MessageFormatException;
import cn.timd.Jelery.MessageBroker;
import cn.timd.Jelery.Vo.TaskMessageVo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class MemoryMessageBroker implements MessageBroker {
    private final static Gson gson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create();
    private Queue<byte[]> queue = new LinkedBlockingQueue<byte[]>();

    public void sendMessage(TaskMessageVo taskMessage, int timeoutMS) throws MessageBrokerException {
        if (taskMessage != null)
            queue.add(gson.toJson(taskMessage).getBytes());
    }

    public synchronized TaskMessageVo getMessage(int timeoutMS)
            throws MessageBrokerException, MessageFormatException {
        if (queue.isEmpty())
            return null;
        try {
            return gson.fromJson(new String(queue.remove()), TaskMessageVo.class);
        } catch (JsonSyntaxException ex) {
            throw new MessageFormatException(ex.getMessage());
        }
    }
}
