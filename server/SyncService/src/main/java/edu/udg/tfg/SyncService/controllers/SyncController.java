package edu.udg.tfg.SyncService.controllers;

import edu.udg.tfg.SyncService.Entities.mappers.CommandMapper;
import edu.udg.tfg.SyncService.controllers.requests.CommandRequest;
import edu.udg.tfg.SyncService.services.CommandService;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.UUID;

@Controller("/websocket")
public class SyncController {

    @Autowired
    private CommandService commandService;

    @Autowired
    private CommandMapper commandMapper;

    @MessageMapping("/sync")
    public ResponseEntity<?> synchronizeClient(@RequestHeader("X-User-Id") UUID userId, @RequestBody List<CommandRequest> clientCommands) {
        if (!clientCommands.isEmpty()) {
            commandService.synchronizeClientCommands(userId, commandMapper.map(clientCommands));
        }
        return ResponseEntity.ok().build();
    }

    @MessageMapping("/ack/{commandId}")
    public ResponseEntity<?> acknowledgeCommand(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID commandId) {
        commandService.removeCommand(userId, commandId);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleRuntimeException(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Element not found");
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<String> handleForbiddenException(ForbiddenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this resource");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
}
