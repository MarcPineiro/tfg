package edu.udg.tfg.FileSharing.queue;

import edu.udg.tfg.FileSharing.config.RabbitConfig;
import edu.udg.tfg.FileSharing.queue.messages.DeleteElement;
import edu.udg.tfg.FileSharing.queue.messages.DeleteElemtnConfirm;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class Sender {

    @Autowired
    private AmqpTemplate rabbitTemplate;

    @Value("${spring.application.name}")
    String serviceName;

    public void confirmDelete(DeleteElement deleteElement) {
        DeleteElemtnConfirm deleteElemtnConfirm = new DeleteElemtnConfirm(deleteElement.userId(), deleteElement.elementId(), serviceName);
        rabbitTemplate.convertAndSend(RabbitConfig.CONFIRM_DELETE, deleteElemtnConfirm);
    }
}
