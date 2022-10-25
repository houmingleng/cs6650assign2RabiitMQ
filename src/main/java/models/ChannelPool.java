package models;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.*;

public class ChannelPool {
    private Connection connection;
    private BlockingQueue<Channel> pool;
    private final static String QUEUE_NAME = "rpc_queue";

    public ChannelPool() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        //factory.setVirtualHost("6650");
       // factory.setUsername("");
       // factory.setPassword("");
//        factory.setHost("localhost");
        this.connection = factory.newConnection();
        this.pool = new LinkedBlockingQueue<>();
        int i = 0;
        while(i++ < 600) {
            Channel channel = connection.createChannel();
            pool.add(channel);
        }
        System.out.println(pool.size());
    }

    public Channel getChannel() throws IOException, InterruptedException {
        Channel channel = pool.poll(100, TimeUnit.MILLISECONDS);
        if(channel == null) {
            channel = connection.createChannel();
        }
        return channel;
    }

    public void add(Channel channel) {
        pool.add(channel);
    }
    public BlockingQueue<Channel> getPool() {
        return pool;
    }
}
