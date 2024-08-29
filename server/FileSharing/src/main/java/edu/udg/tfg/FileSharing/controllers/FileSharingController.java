package edu.udg.tfg.FileSharing.controllers;

import edu.udg.tfg.FileSharing.controllers.requests.SharedRequest;
import edu.udg.tfg.FileSharing.entities.mappers.SharedAccessMapper;
import edu.udg.tfg.FileSharing.services.FileSharingService;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping
public class FileSharingController {

    @Autowired
    private FileSharingService fileSharingService;

    @Autowired
    private SharedAccessMapper sharedAccessMapper;

    @PostMapping("/{userId}")
    public ResponseEntity<?> shareFile(@PathVariable UUID userId, @RequestBody SharedRequest sharedRequest) {
        fileSharingService.shareFile(sharedRequest, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getSharedFiles(@PathVariable UUID userId) {
        return ResponseEntity.ok(
                sharedAccessMapper.map(fileSharingService.getSharedFiles(userId))
        );
    }

    @GetMapping("/user/{elementId}")
    public ResponseEntity<?> getUsersShared(@PathVariable UUID elementId) {
        return ResponseEntity.ok(
                sharedAccessMapper.mapUsers(fileSharingService.getSharedUsers(elementId))
        );
    }

    @DeleteMapping("/{fileId}/{userId}")
    public ResponseEntity<Void> revokeSharedFile(@PathVariable UUID userId, @RequestBody SharedRequest sharedRequest) {
        fileSharingService.revokeSharedFile(sharedRequest, userId);
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