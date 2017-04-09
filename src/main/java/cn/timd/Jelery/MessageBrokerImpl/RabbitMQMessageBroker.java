package cn.timd.Jelery.MessageBrokerImpl;

import cn.timd.Jelery.Exception.MessageBrokerException;
import cn.timd.Jelery.Exception.MessageBrokerTimeoutException;
import cn.timd.Jelery.Exception.MessageFormatException;
import cn.timd.Jelery.MessageBroker;
import cn.timd.Jelery.Vo.TaskMessageVo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQMessageBroker implements MessageBroker {
    private final static Gson gson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    private ConnectionFactory factory;
    private Connection publishConnection;
    private Channel publishChannel;
    private String exchangeName;
    private String queueName;
    private String routingKey;

    private int qos = 1;
    private Connection consumeConnection;
    private Channel consumeChannel;
    private QueueingConsumer consumer;

    public RabbitMQMessageBroker(String host, int port,
                                 String username, String password,
                                 String virtualHost, String queueName,
                                 String exchangeName, String routingKey) {
        factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);

        this.exchangeName = exchangeName;
        this.queueName = queueName;
        this.routingKey = routingKey;
    }

    public RabbitMQMessageBroker setQos(int qos) {
        if (qos > 0)
            this.qos = qos;
        return this;
    }

    private synchronized Channel getPublishChannel()
            throws IOException, TimeoutException {
        if (publishChannel != null)
            return publishChannel;

        publishChannel = (publishConnection = factory.newConnection()).createChannel();
        publishChannel.exchangeDeclare(exchangeName, "direct", true);
        publishChannel.queueDeclare(queueName, true, false, false, null);
        publishChannel.queueBind(queueName, exchangeName, routingKey);
        return publishChannel;
    }

    private synchronized Channel getConsumeChannel()
            throws IOException, TimeoutException {
        if (consumeChannel != null)
            return consumeChannel;

        consumeChannel = (consumeConnection = factory.newConnection()).createChannel();
        consumeChannel.exchangeDeclare(exchangeName, "direct", true);
        consumeChannel.queueDeclare(queueName, true, false, false, null);
        consumeChannel.queueBind(queueName, exchangeName, routingKey);

        consumeChannel.basicQos(qos);
        consumer = new QueueingConsumer(consumeChannel);
        consumeChannel.basicConsume(queueName, false, consumer);
        return consumeChannel;
    }

    public void sendMessage(TaskMessageVo taskMessage, int timeoutMS) throws MessageBrokerException {
        try {
            publishChannel = getPublishChannel();
            AMQP.BasicProperties properties =
                    new AMQP.BasicProperties.Builder()
                            .contentType("application/json")
                            .deliveryMode(2)
                            .build();
            publishChannel.txSelect();
            try {
                publishChannel.basicPublish(exchangeName, routingKey,
                        properties, gson.toJson(taskMessage).getBytes());
            } catch (IOException ex) {
                publishChannel.txRollback();
                throw ex;
            }
            publishChannel.txCommit();
        } catch (IOException ex) {
            ex.printStackTrace();
            closePublishConnection();
            throw new MessageBrokerException(ex.getMessage());
        } catch (ShutdownSignalException ex) {
            ex.printStackTrace();
            closePublishConnection();
            throw new MessageBrokerException(ex.getMessage());
        } catch (TimeoutException ex) {
            ex.printStackTrace();
            throw new MessageBrokerTimeoutException(ex.getMessage());
        }
    }

    public TaskMessageVo getMessage(int timeoutMS)
            throws MessageBrokerException, MessageFormatException {
        byte[] body;

        try {
            consumeChannel= getConsumeChannel();
            QueueingConsumer.Delivery delivery = consumer.nextDelivery(timeoutMS);
            if (delivery == null)
                throw new MessageBrokerTimeoutException("timeout");
            consumeChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            body = delivery.getBody();
        } catch (IOException ex) {
            ex.printStackTrace();
            closeConsumeConnection();
            throw new MessageBrokerException(ex.getMessage());
        } catch (ShutdownSignalException ex) {
            ex.printStackTrace();
            closeConsumeConnection();
            throw new MessageBrokerException(ex.getMessage());
        }
        catch (TimeoutException ex) {
            ex.printStackTrace();
            throw new MessageBrokerTimeoutException(ex.getMessage());
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return null;
        }

        try {
            return gson.fromJson(new String(body), TaskMessageVo.class);
        } catch (JsonSyntaxException ex) {
            ex.printStackTrace();
            throw new MessageFormatException(ex.getMessage());
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            throw new MessageFormatException(ex.getMessage());
        }
    }

    private synchronized void closeConsumeConnection() {
        try {
            if (consumeChannel != null)
                consumeChannel.close();
            if (consumeConnection != null)
                consumeConnection.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            consumeChannel = null;
            consumeConnection = null;
        }
    }

    private synchronized void closePublishConnection() {
        try {
            if (publishChannel != null)
                publishChannel.close();
            if (publishConnection != null)
                publishConnection.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            publishChannel = null;
            publishConnection = null;
        }
    }

    public void close() {
        closePublishConnection();
        closeConsumeConnection();
    }
}
