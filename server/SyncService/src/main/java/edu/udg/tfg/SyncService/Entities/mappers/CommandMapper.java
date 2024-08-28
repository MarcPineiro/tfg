package edu.udg.tfg.SyncService.Entities.mappers;

import java.util.List;
import java.util.UUID;

import edu.udg.tfg.SyncService.Entities.CommandEntity;
import edu.udg.tfg.SyncService.controllers.requests.CommandRequest;
import edu.udg.tfg.SyncService.queue.messages.CommandRabbit;
import edu.udg.tfg.SyncService.websocket.messages.CommandMessage;
import edu.udg.tfg.SyncService.websocket.messages.WebCommandMessage;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CommandMapper {

    List<CommandEntity> map(List<CommandRequest> clientCommands);

    @Mapping(source = "elementId", target = "elementId")
    CommandRequest map(CommandEntity source);
    @Mapping(source = "elementId", target = "elementId")
    CommandEntity map(CommandRequest source);
    @Mapping(source = "elementId", target = "elementId")
    CommandEntity map(CommandRabbit source);
    @Mapping(source = "elementId", target = "elementId")
    CommandMessage mapMessage(CommandEntity source);
    @Mapping(source = "elementId", target = "elementId")
    WebCommandMessage mapWebMessage(CommandEntity source);

    default String map(UUID value) {
        return value != null ? value.toString() : null;
    }

    default UUID map(String value) {
        return value != null ? UUID.fromString(value) : null;
    }

}
