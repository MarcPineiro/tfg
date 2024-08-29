package edu.udg.tfg.FileManagement.queue;

import edu.udg.tfg.FileManagement.config.RabbitConfig;
import edu.udg.tfg.FileManagement.feignClients.fileAccess.AccessType;
import edu.udg.tfg.FileManagement.queue.messages.AddAccess;
import edu.udg.tfg.FileManagement.queue.messages.DeleteAccess;
import edu.udg.tfg.FileManagement.queue.messages.DeleteElemtn;
import edu.udg.tfg.FileManagement.queue.messages.DeleteElemtnConfirm;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class Sender {

    @Autowired
    private AmqpTemplate rabbitTemplate;

    @Value("${spring.application.name}")
    String serviceName;

    public void deleteAccess(UUID elementId, UUID userId) {
        DeleteAccess message = new DeleteAccess(userId, elementId);
        rabbitTemplate.convertAndSend(RabbitConfig.DELETE_ACCESS_QUEUE, message);
    }

    public void addAccess(UUID elementId, UUID userId, AccessType accessType) {
        AddAccess message = new AddAccess(userId, elementId, accessType.ordinal());
        rabbitTemplate.convertAndSend(RabbitConfig.ADD_ACCESS_QUEUE, message);
    }

    public void confirmDelete(DeleteElemtn deleteElemtn) {
        DeleteElemtnConfirm deleteElemtnConfirm = new DeleteElemtnConfirm(deleteElemtn.userId(), deleteElemtn.elementId(), serviceName);
        rabbitTemplate.convertAndSend(RabbitConfig.CONFIRM_DELETE, deleteElemtnConfirm);
    }
}
