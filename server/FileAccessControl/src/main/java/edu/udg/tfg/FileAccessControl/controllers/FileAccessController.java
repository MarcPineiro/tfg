package edu.udg.tfg.FileAccessControl.controllers;// FileAccessController.java

import edu.udg.tfg.FileAccessControl.controllers.requests.AccessRequest;
import edu.udg.tfg.FileAccessControl.entities.mappers.AccessMapper;
import edu.udg.tfg.FileAccessControl.services.FileAccessService;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping
public class FileAccessController {

    @Autowired
    private FileAccessService fileAccessService;

    @Autowired
    private AccessMapper accessMapper;

    @PostMapping("/")
    public ResponseEntity<?> addFileAccess(@RequestBody AccessRequest fileAccess) {
        fileAccessService.addFileAccess(accessMapper.map(fileAccess));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{fileId}/{userId}")
    public ResponseEntity<?> deleteFileAccess(@PathVariable UUID fileId, @PathVariable UUID userId) {
        fileAccessService.deleteFileAccess(fileId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{fileId}/{userId}")
    public ResponseEntity<?> getFileAccess(@PathVariable UUID fileId, @PathVariable UUID userId) {
        return ResponseEntity.ok(
                accessMapper.map(fileAccessService.getFileAccess(fileId, userId))
        );
    }

    @GetMapping("/list/{userId}")
    public ResponseEntity<?> getFileAccessByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(
                accessMapper.map(fileAccessService.getFileAccessByUserId(userId))
        );
    }

    @GetMapping("/list/{fileId}")
    public ResponseEntity<?> getFileAccessByFileId(@PathVariable UUID fileId) {
        return ResponseEntity.ok(
                accessMapper.map(fileAccessService.getFileAccessByFileId(fileId))
        );
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
