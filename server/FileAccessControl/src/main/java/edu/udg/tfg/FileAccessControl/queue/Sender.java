package edu.udg.tfg.FileAccessControl.queue;

import edu.udg.tfg.FileAccessControl.config.RabbitConfig;
import edu.udg.tfg.FileAccessControl.queue.messages.DeleteElemtn;
import edu.udg.tfg.FileAccessControl.queue.messages.DeleteElemtnConfirm;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class Sender {

    @Autowired
    private AmqpTemplate rabbitTemplate;

    @Value("${spring.application.name}")
    String serviceName;

    public void confirmDelete(DeleteElemtn deleteElemtn) {
        DeleteElemtnConfirm deleteElemtnConfirm = new DeleteElemtnConfirm(deleteElemtn.userId(), deleteElemtn.elementId(), serviceName);
        rabbitTemplate.convertAndSend(RabbitConfig.CONFIRM_DELETE, deleteElemtnConfirm);
    }
}
