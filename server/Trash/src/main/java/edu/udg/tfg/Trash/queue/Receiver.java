package edu.udg.tfg.Trash.queue;

import edu.udg.tfg.Trash.queue.messages.DeleteElemtnConfirm;
import edu.udg.tfg.Trash.services.TrashService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.udg.tfg.Trash.config.RabbitConfig;

@Component
public class Receiver {

    @Autowired
    private TrashService trashService;

    @RabbitListener(queues = RabbitConfig.CONFIRM_DELETE)
    public void confirmDeleteAccess(DeleteElemtnConfirm deleteElemtnConfirm) {
        trashService.confirm(deleteElemtnConfirm.elementId(), deleteElemtnConfirm.userId(), deleteElemtnConfirm.service());
    }

}