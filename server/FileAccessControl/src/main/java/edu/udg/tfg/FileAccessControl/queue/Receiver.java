package edu.udg.tfg.FileAccessControl.queue;

import edu.udg.tfg.FileAccessControl.config.RabbitConfig;
import edu.udg.tfg.FileAccessControl.entities.AccessRule;
import edu.udg.tfg.FileAccessControl.entities.AccessType;
import edu.udg.tfg.FileAccessControl.queue.messages.AddAccess;
import edu.udg.tfg.FileAccessControl.queue.messages.DeleteAccess;
import edu.udg.tfg.FileAccessControl.repositories.AccessRuleRepository;
import edu.udg.tfg.FileAccessControl.services.FileAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

@Component
public class Receiver {
    @Autowired
    FileAccessService fileAccessService;

    @RabbitListener(queues = RabbitConfig.DELETE_ACCESS_QUEUE)
    public void receiveMessage(final DeleteAccess deleteAccess) {
        fileAccessService.deleteFileAccess(deleteAccess.elementId(), deleteAccess.userId());
    }

    @RabbitListener(queues = RabbitConfig.DELETE_USER_FA)
    public void receiveMessage(final UUID userId) {
        fileAccessService.deleteByUserId(userId);
    }

    @RabbitListener(queues = RabbitConfig.ADD_ACCESS_QUEUE)
    public void receiveMessage(final AddAccess addAccess) {
        AccessRule accessRule = new AccessRule();
        accessRule.setElementId(addAccess.elementId());
        accessRule.setUserId(addAccess.userId());
        accessRule.setAccessType(AccessType.values()[addAccess.accessType()]);
        fileAccessService.addFileAccess(accessRule);
    }


}