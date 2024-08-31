package edu.udg.tfg.UserAuthentication.queue;

import edu.udg.tfg.UserAuthentication.config.RabbitConfig;
import edu.udg.tfg.UserAuthentication.services.UserService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@Component
public class Receiver {
    @Autowired
    private UserService userService;

    @RabbitListener(queues = RabbitConfig.DELETE_USER_UA)
    public void receiveMessage(final UUID userId) {
        userService.deleteByUserId(userId);
    }
}