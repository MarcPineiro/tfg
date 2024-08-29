package edu.udg.tfg.FileSharing.queue;

import edu.udg.tfg.FileSharing.queue.messages.DeleteElement;
import edu.udg.tfg.FileSharing.services.FileSharingService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.udg.tfg.FileSharing.config.RabbitConfig;

import java.util.UUID;

@Component
public class Receiver {

    @Autowired
    private FileSharingService fileSharingService;

    @Autowired
    private Sender sender;

    @RabbitListener(queues = RabbitConfig.DELTE_SHARING)
    public void deleteElement(DeleteElement deleteElement) {
        fileSharingService.delete(UUID.fromString(deleteElement.elementId()), UUID.fromString(deleteElement.userId()));
        sender.confirmDelete(deleteElement);
    }
}