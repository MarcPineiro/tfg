package edu.udg.tfg.FileManagement.controlllers;

import com.netflix.discovery.converters.Auto;
import edu.udg.tfg.FileManagement.controlllers.responses.FolderInfo;
import edu.udg.tfg.FileManagement.controlllers.responses.FolderStructure;
import edu.udg.tfg.FileManagement.entities.FileEntity;
import edu.udg.tfg.FileManagement.entities.FolderEntity;
import edu.udg.tfg.FileManagement.entities.mappers.FileMapper;
import edu.udg.tfg.FileManagement.entities.mappers.FolderMapper;
import edu.udg.tfg.FileManagement.feignClients.fileAccess.AccessType;
import edu.udg.tfg.FileManagement.services.*;
import feign.FeignException;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/trash")
public class TrashController {
    @Autowired
    private FolderService folderService;

    @Autowired
    private FileAccessService fileAccessService;

    @Autowired
    private TrashService trashService;

    @Autowired
    private ElementService elementService;

    @Autowired
    private FileService fileService;

    @Autowired
    private FolderMapper folderMapper;

    @Autowired
    private FileMapper fileMapper;

    @GetMapping("/root")
    public ResponseEntity<?> getRootFolder(@RequestHeader("X-User-Id") UUID userId) {
        FolderInfo folderInfo = trashService.getRootByUserId(userId);
        return ResponseEntity.ok(folderInfo);
    }

    @DeleteMapping("/{elementId}")
    public ResponseEntity<?> remove(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID elementId) {
        boolean isFolder = elementService.isFolder(elementId);
        fileAccessService.checkAccessElement(userId, elementId, isFolder, AccessType.ADMIN);
        trashService.remove(elementId, userId, isFolder);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{elementId}")
    public ResponseEntity<?> getElement(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID elementId) {
        boolean isFolder = elementService.isFolder(elementId);
        fileAccessService.checkAccessElement(userId, elementId, isFolder, AccessType.WRITE);
        if (isFolder) {
            FolderEntity folder = folderService.getFolderByElementId(elementId, true);
            return ResponseEntity.ok().body(folderMapper.mapFileInfo(folder));
        } else {
            FileEntity file = fileService.getFolderByElementId(elementId, true);
            return ResponseEntity.ok().body(fileMapper.map(file));
        }
    }

    @PutMapping("/{elementId}/restore")
    public ResponseEntity<?> restore(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID elementId) {
        boolean isFolder = elementService.isFolder(elementId);
        fileAccessService.checkAccessElement(userId, elementId, isFolder, AccessType.ADMIN);
        trashService.restore(elementId, userId);
        if(isFolder) {
            folderService.restore(elementId);
        } else {
            fileService.restore(elementId);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/structure")
    public ResponseEntity<?> getFolderStructure(@RequestHeader("X-User-Id") UUID userId) {
        FolderStructure structure = trashService.getFolderStructure(userId);
        return ResponseEntity.ok(structure);
    }

    // Exception handling
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleRuntimeException(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Element not found");
    }

    // Exception handling
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<String> handleRuntimeException(FeignException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error in communication with trash");
    }

    // Exception handling
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
}
