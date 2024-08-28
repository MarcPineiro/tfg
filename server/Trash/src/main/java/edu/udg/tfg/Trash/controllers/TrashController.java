package edu.udg.tfg.Trash.controllers;// FileAccessController.java

import edu.udg.tfg.Trash.controllers.requests.TrashRequest;
import edu.udg.tfg.Trash.controllers.responses.TrashResponse;
import edu.udg.tfg.Trash.entities.mappers.TrashRecordMapper;
import edu.udg.tfg.Trash.services.TrashService;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping
public class TrashController {

    @Autowired
    private TrashService trashService;
    
    @Autowired
    TrashRecordMapper trashRecordMapper;

    @PostMapping("/{userId}")
    public ResponseEntity<?> addRecord(@PathVariable UUID userId, @RequestBody TrashRequest trashRequest){
        trashService.addRecord(userId, trashRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<TrashResponse> getTrashFiles(@PathVariable UUID userId) {
        return ResponseEntity.ok(
                trashService.getSharedFiles(userId)
        );
    }

    @DeleteMapping("/{userId}/{elementId}")
    public ResponseEntity<?> restoreFile(@PathVariable UUID userId, @PathVariable UUID elementId) {
        trashService.remove(userId, elementId);
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
