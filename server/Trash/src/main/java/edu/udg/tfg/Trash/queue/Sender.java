package edu.udg.tfg.Trash.queue;

import edu.udg.tfg.Trash.config.RabbitConfig;
import edu.udg.tfg.Trash.queue.messages.DeleteElemtn;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;


@Component
public class Sender {

    @Autowired
    private AmqpTemplate rabbitTemplate;

    public void removeAccess(UUID userId, UUID elementId) {
        DeleteElemtn deleteElemtn = new DeleteElemtn(userId, elementId);
        rabbitTemplate.convertAndSend(RabbitConfig.DELETE_ACCESS_QUEUE, deleteElemtn);
    }

    public void removeSharing(UUID userId, UUID elementId) {
        DeleteElemtn deleteElemtn = new DeleteElemtn(userId, elementId);
        rabbitTemplate.convertAndSend(RabbitConfig.DELTE_SHARING, deleteElemtn);
    }

    public void removeManagement(UUID userId, UUID elementId) {
        DeleteElemtn deleteElemtn = new DeleteElemtn(userId, elementId);
        rabbitTemplate.convertAndSend(RabbitConfig.DELETE_ELEMENT, deleteElemtn);
    }

}
