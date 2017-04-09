package cn.timd.Jelery.ResultBackendImpl;

import cn.timd.Jelery.Exception.ResultBackendException;
import cn.timd.Jelery.Exception.ResultFormatException;
import cn.timd.Jelery.ResultBackend;
import cn.timd.Jelery.Vo.TaskResultVo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

public class RedisResultBackend implements ResultBackend {
    private final static Gson gson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    private String host;
    private int port;
    private int timeoutMS;
    private String password;
    private int db;
    private JedisPool jedisPool;

    public RedisResultBackend(String host, int port, int timeoutMS, String password, int db) {
        this.host = host;
        this.port = port;
        this.timeoutMS = timeoutMS;
        this.password = password;
        this.db = db;
    }

    private synchronized Jedis getJedis() {
        if (jedisPool == null)
            jedisPool = new JedisPool(new JedisPoolConfig(), host, port, timeoutMS, password, db);
        return jedisPool.getResource();
    }

    public TaskResultVo getResult(String taskID, int timeoutMS)
            throws ResultBackendException, ResultFormatException {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return gson.fromJson(jedis.get(taskID), TaskResultVo.class);
        } catch (JedisException ex) {
            ex.printStackTrace();
            throw new ResultBackendException(ex.getMessage());
        } catch (JsonSyntaxException ex) {
            ex.printStackTrace();
            throw new ResultFormatException(ex.getMessage());
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            throw new ResultFormatException(ex.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }

    public void setResult(String taskID, TaskResultVo taskResult, int timeoutMS, int expireTimeoutMS)
            throws ResultBackendException {
        if (taskID == null || taskResult == null)
            return;

        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.set(taskID, gson.toJson(taskResult));
            jedis.expire(taskID, expireTimeoutMS / 1000);
        } catch (JedisException ex) {
            ex.printStackTrace();
            throw new ResultBackendException(ex.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }

    public synchronized void close() {
        try {
            if (jedisPool != null)
                jedisPool.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jedisPool = null;
        }
    }
}
