package edu.udg.tfg.SyncService.services;


import edu.udg.tfg.SyncService.Entities.CommandEntity;
import edu.udg.tfg.SyncService.Entities.mappers.CommandMapper;
import edu.udg.tfg.SyncService.controllers.requests.CommandRequest;
import edu.udg.tfg.SyncService.repositories.CommandRepository;
import edu.udg.tfg.SyncService.websocket.messages.CommandMessage;
import edu.udg.tfg.SyncService.websocket.messages.WebCommandMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommandService {

    @Autowired
    private CommandRepository commandRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private WebSocketConnectionService webSocketConnectionService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private CommandMapper commandMapper;

    private static final String DESKTOP_ROUTE = "/queue/desktop/reply";
    private static final String WEB_ROUTE = "/queue/web/reply";


    public void handleWebAppCommand(UUID userId, CommandEntity command) {
        if (webSocketConnectionService.isDesktopClientConnected(userId)) {
            sendCommandToDesktop(userId.toString(), commandMapper.mapMessage(command));
        } else {
            commandRepository.deleteByElementIdAndUserId(command.getElementId(), userId);
            commandRepository.save(command);
        }
    }

    public void handleDesktopAppCommand(UUID userId, CommandEntity command) {
        sendCommandToWeb(userId.toString(), commandMapper.mapWebMessage(command));
    }

    public List<CommandEntity> getPendingCommands(UUID userId) {
        return commandRepository.findByUserId(userId);
    }

    private void sendCommandToDesktop(String userId, CommandMessage command) {
        messagingTemplate.convertAndSendToUser(
                userId,
                CommandService.DESKTOP_ROUTE,
                command
        );
    }

    private void sendCommandToWeb(String userId, WebCommandMessage command) {
        messagingTemplate.convertAndSendToUser(
                userId,
                CommandService.WEB_ROUTE,
                command
        );
    }

    public void synchronizeClientCommands(UUID userId, List<CommandEntity> clientCommands) {
        List<CommandEntity> serverCommands = getPendingCommands(userId);

        for (CommandEntity clientCommand : clientCommands) {
            Optional<CommandEntity> existingCommand = serverCommands.stream()
                    .filter(cmd -> cmd.getElementId().equals(clientCommand.getElementId()))
                    .findFirst();

            if (existingCommand.isPresent()) {
                if (clientCommand.getLastModificationDate().after(existingCommand.get().getLastModificationDate())) {
                    commandRepository.deleteByElementIdAndUserId(existingCommand.get().getElementId(), userId);
                    commandRepository.save(clientCommand);
                }
            } else {
                commandRepository.save(clientCommand);
            }
        }

        for (CommandEntity command : serverCommands) {
            if (webSocketConnectionService.isDesktopClientConnected(userId)) {
                sendCommandToDesktop(userId.toString(), commandMapper.mapMessage(command));
            }
        }
    }

    public void removeCommand(UUID userId, UUID commandId) {
        commandRepository.deleteById(commandId);
    }
}
