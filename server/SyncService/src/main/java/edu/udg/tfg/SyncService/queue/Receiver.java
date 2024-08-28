package edu.udg.tfg.SyncService.queue;

import com.netflix.discovery.converters.Auto;
import edu.udg.tfg.SyncService.Entities.mappers.CommandMapper;
import edu.udg.tfg.SyncService.config.RabbitConfig;
import edu.udg.tfg.SyncService.controllers.WebSocketController;
import edu.udg.tfg.SyncService.controllers.requests.CommandRequest;
import edu.udg.tfg.SyncService.queue.messages.CommandRabbit;
import edu.udg.tfg.SyncService.services.CommandService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class Receiver {

    @Autowired
    private CommandService commandService;

    @Autowired
    private CommandMapper commandMapper;

    @RabbitListener(queues = RabbitConfig.COMMAND_QUEUE)
    public void processWebAppCommand(CommandRabbit command) {
        if("web".equals(command.client())) {
            commandService.handleWebAppCommand(UUID.fromString(command.userId()), commandMapper.map(command));
        } else {
            commandService.handleDesktopAppCommand(UUID.fromString(command.userId()), commandMapper.map(command));
        }
    }
}
