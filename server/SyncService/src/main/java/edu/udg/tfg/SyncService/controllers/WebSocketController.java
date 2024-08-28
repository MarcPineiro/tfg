package edu.udg.tfg.SyncService.controllers;

import com.netflix.discovery.converters.Auto;
import edu.udg.tfg.SyncService.Entities.mappers.CommandMapper;
import edu.udg.tfg.SyncService.controllers.requests.CommandRequest;
import edu.udg.tfg.SyncService.services.CommandService;
import edu.udg.tfg.SyncService.services.WebSocketConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class WebSocketController {


}
