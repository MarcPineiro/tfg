package edu.udg.tfg.UserManagement.queue;

import edu.udg.tfg.UserManagement.config.RabbitConfig;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;


@Component
public class Sender {

    @Autowired
    private AmqpTemplate rabbitTemplate;

    public void send() {
        String context = "hello " + new Date();
        System.out.println("Sender : " + context);
        rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_NAME, "Hello from RabbitMQ!");
    }

    public void deleteUser(UUID id) {
        rabbitTemplate.convertAndSend(RabbitConfig.DELETE_USER_TR, id);
        rabbitTemplate.convertAndSend(RabbitConfig.DELETE_USER_FM, id);
        rabbitTemplate.convertAndSend(RabbitConfig.DELETE_USER_FS, id);
        rabbitTemplate.convertAndSend(RabbitConfig.DELETE_USER_FA, id);
        rabbitTemplate.convertAndSend(RabbitConfig.DELETE_USER_UA, id);
        rabbitTemplate.convertAndSend(RabbitConfig.DELETE_USER_SS, id);
    }
}
