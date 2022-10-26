package comsumer;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import models.LiftRide;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SkiersConsumer {
    private final static String QUEUE_NAME = "rpc_queue";
    private final static Integer THREADS = 600;

    public static void main(String[] args) throws Exception{
        ConcurrentHashMap<String, CopyOnWriteArrayList<LiftRide>> map = new ConcurrentHashMap<>();
        ConnectionFactory factory = new ConnectionFactory();
        Gson gson = new Gson();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Runnable runnable = () -> {
            Channel channel;
            try{
                channel = connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    LiftRide liftRide = gson.fromJson(message, LiftRide.class);
                    System.out.println(" [x] Received '" + liftRide.toString() + "'");
                    map.putIfAbsent(liftRide.getSkierID(), new CopyOnWriteArrayList<>());
                    map.get(liftRide.getSkierID()).add(liftRide);
                };
                channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        for (int i = 0; i < THREADS; i++) {
            Thread thread = new Thread(runnable);
            thread.start();
        }
    }
}
