package edu.udg.tfg.FileManagement.controlllers;

import edu.udg.tfg.FileManagement.controlllers.requests.ShareRequest;
import edu.udg.tfg.FileManagement.controlllers.responses.FolderInfo;
import edu.udg.tfg.FileManagement.controlllers.responses.FolderStructure;
import edu.udg.tfg.FileManagement.feignClients.fileAccess.AccessType;
import edu.udg.tfg.FileManagement.services.ElementService;
import edu.udg.tfg.FileManagement.services.FileAccessService;
import edu.udg.tfg.FileManagement.services.FolderService;
import edu.udg.tfg.FileManagement.services.SharedService;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/share")
public class SharedController {
    @Autowired
    private FolderService folderService;

    @Autowired
    private FileAccessService fileAccessService;

    @Autowired
    private SharedService sharedService;

    @Autowired
    private ElementService elementService;

    @GetMapping("/root")
    public ResponseEntity<?> getRootFolder(@RequestHeader("X-User-Id") UUID userId) {
        FolderInfo folderInfo = sharedService.getRootByUserId(userId);
        return ResponseEntity.ok(folderInfo);
    }

    @PostMapping("/")
    public ResponseEntity<?> share(@RequestHeader("X-User-Id") UUID userId, @RequestBody ShareRequest shareRequest) {
        fileAccessService.checkAccessElement(userId, shareRequest.getElementId(), !shareRequest.isFile(), AccessType.ADMIN);
        sharedService.share(shareRequest, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/structure")
    public ResponseEntity<?> getFolderStructure(@RequestHeader("X-User-Id") UUID userId) {
        FolderStructure structure = sharedService.getFolderStructure(userId);
        return ResponseEntity.ok(structure);
    }

    @DeleteMapping("/{elementId}")
    public ResponseEntity<?> revokeShare(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID elementId) {
        boolean isFolder = elementService.isFolder(elementId);
        fileAccessService.checkAccessElement(userId, elementId, isFolder, AccessType.ADMIN);
        sharedService.revokeShare(elementId, userId, isFolder);
        return ResponseEntity.ok().build();
    }

    // Exception handling
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleRuntimeException(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Element not found");
    }

    // Exception handling
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
}
