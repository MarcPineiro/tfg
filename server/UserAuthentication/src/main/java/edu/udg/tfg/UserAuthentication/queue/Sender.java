package edu.udg.tfg.UserAuthentication.queue;

import edu.udg.tfg.UserAuthentication.config.RabbitConfig;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class Sender {

    private final AmqpTemplate rabbitTemplate;

    public Sender(AmqpTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send() {
        String context = "hello " + new Date();
        System.out.println("Sender : " + context);
        rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_NAME, "Hello from RabbitMQ!");
    }

}
