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
    private Connection connection;
    private Channel channel;
    private QueueingConsumer consumer;
    private String exchangeName;
    private String queueName;
    private String routingKey;

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

    private synchronized Channel getChannel() throws IOException, TimeoutException {
        if (channel != null)
            return channel;

        channel = (connection = factory.newConnection()).createChannel();
        channel.basicQos(1);
        channel.exchangeDeclare(exchangeName, "direct", true);
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, exchangeName, routingKey);

        consumer = new QueueingConsumer(channel);
        return channel;
    }

    public void sendMessage(TaskMessageVo taskMessage, int timeoutMS) throws MessageBrokerException {
        try {
            channel = getChannel();
            AMQP.BasicProperties properties =
                    new AMQP.BasicProperties.Builder()
                            .contentType("application/json")
                            .deliveryMode(2)
                            .build();
            channel.txSelect();
            try {
                channel.basicPublish(exchangeName, routingKey,
                        properties, gson.toJson(taskMessage).getBytes());
            } catch (IOException ex) {
                channel.txRollback();
                throw ex;
            }
            channel.txCommit();
        } catch (IOException ex) {
            ex.printStackTrace();
            channel = null;
            throw new MessageBrokerException(ex.getMessage());
        } catch (TimeoutException ex) {
            ex.printStackTrace();
            throw new MessageBrokerTimeoutException(ex.getMessage());
        }
    }

    public TaskMessageVo getMessage(int timeoutMS)
            throws MessageBrokerException, MessageFormatException {
        boolean autoAck = true;
        byte[] body;

        try {
            channel = getChannel();
            channel.basicConsume(queueName, autoAck, consumer);
            QueueingConsumer.Delivery delivery = consumer.nextDelivery(timeoutMS);
            if (delivery == null)
                throw new MessageBrokerTimeoutException("timeout");
            body = delivery.getBody();
        } catch (IOException ex) {
            ex.printStackTrace();
            channel = null;
            throw new MessageBrokerException(ex.getMessage());
        } catch (TimeoutException ex) {
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

    public synchronized void close() {
        try {
            if (channel != null)
                channel.close();
            if (connection != null)
                connection.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            channel = null;
            connection = null;
        }
    }
}
