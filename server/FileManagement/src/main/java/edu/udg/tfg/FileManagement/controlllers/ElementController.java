package edu.udg.tfg.FileManagement.controlllers;

import com.netflix.discovery.converters.Auto;
import edu.udg.tfg.FileManagement.controlllers.requests.CreateFileRequest;
import edu.udg.tfg.FileManagement.controlllers.requests.UpdateFileRequest;
import edu.udg.tfg.FileManagement.controlllers.responses.FileInfo;
import edu.udg.tfg.FileManagement.controlllers.responses.FolderStructure;
import edu.udg.tfg.FileManagement.entities.FileEntity;
import edu.udg.tfg.FileManagement.entities.FolderEntity;
import edu.udg.tfg.FileManagement.entities.mappers.FileMapper;
import edu.udg.tfg.FileManagement.entities.mappers.FolderMapper;
import edu.udg.tfg.FileManagement.feignClients.fileAccess.AccessType;
import edu.udg.tfg.FileManagement.services.*;
import edu.udg.tfg.FileManagement.utils.FileUtil;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class ElementController {

    @Autowired
    private FolderService folderService;
    @Autowired
    private FileService fileService;
    @Autowired
    private ElementService elementService;
    @Autowired
    private FileUtil fileUtil;
    @Autowired
    private FileAccessService fileAccessService;
    @Autowired
    private FolderMapper folderMapper;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private CommandService commandService;

    @GetMapping("/root")
    public ResponseEntity<?> getRootFolder(@RequestHeader("X-User-Id") UUID userId) {
        FolderEntity folder = folderService.getRootFolder(userId, false);
        return ResponseEntity.ok(folderMapper.mapFileInfo(folder));
    }

    @GetMapping("/structure")
    public ResponseEntity<?> getFolderStructure(@RequestHeader("X-User-Id") UUID userId) {
        FolderStructure structure = folderService.getFolderStructure(userId);
        return ResponseEntity.ok(structure);
    }

    @GetMapping("/{elementId}")
    public ResponseEntity<?> getElement(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID elementId) {
        boolean isFolder = elementService.isFolder(elementId);
        fileAccessService.checkAccessElement(userId, elementId, isFolder, AccessType.READ);
        if (isFolder) {
            FolderEntity folder = folderService.getFolderByElementId(elementId, false);
            return ResponseEntity.ok().body(folderService.buildFileInfo(folder));
        } else {
            FileEntity file = fileService.getFolderByElementId(elementId, false);
            return ResponseEntity.ok().body(fileService.buildFileInfo(file));
        }
    }

    @GetMapping("/{elementId}/download")
    public ResponseEntity<?> serveFile(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID elementId) {
        Resource file = null;
        boolean isFolder = elementService.isFolder(elementId);
        fileAccessService.checkAccessElement(userId, elementId, isFolder, AccessType.READ);
        if (isFolder) {
            file = folderService.downloadFolder(elementId);
        } else {
            file = fileUtil.loadAsResource(elementId);
        }
        if (file == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @GetMapping("/{folderId}/files")
    public ResponseEntity<?> getFilesByFolder(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID folderId) {
        FolderEntity folder = folderService.getFolderByElementId(folderId, false);
        if (!fileAccessService.checkAccessFolder(folder, userId, AccessType.READ)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this resource");
        }

        return ResponseEntity.ok(fileMapper.map(folder.getFiles()));
    }

    @GetMapping("/{folderId}/folders")
    public ResponseEntity<?> getFoldersByFolder(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID folderId) {
        FolderEntity folder = folderService.getFolderByElementId(folderId, false);
        if (!fileAccessService.checkAccessFolder(folder, userId, AccessType.READ)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this resource");
        }

        return ResponseEntity.ok(folderMapper.mapFileInfo(folder.getChildren()));
    }

    @PostMapping("/")
    public ResponseEntity<?> createElement(@RequestHeader("X-User-Id") UUID userId, @RequestHeader("X-Client-Type") String clientType, @RequestBody CreateFileRequest request, @Nullable MultipartFile file) {
        fileAccessService.checkAccessElement(userId, request.getParentFolderId(), true, AccessType.WRITE);
        if (request.isFolder()) {
            FolderEntity folder = folderService.createFolder(request.getName(), folderService.getFolderByElementId(request.getParentFolderId(), false), userId);
            return new ResponseEntity<>(folder, HttpStatus.CREATED);
        } else {
            if (file == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            FileEntity fileEntity = fileService.createFile(request.getName(), request.getContentType(), request.getSize(), request.getParentFolderId(), userId);
            try {
                fileUtil.storeFile(fileEntity.getElementId(), file);
            } catch (IOException e) {
                fileService.deleteFile(fileEntity.getElementId());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            return new ResponseEntity<>(fileEntity, HttpStatus.CREATED);
        }
    }

    @PostMapping("/root")
    public ResponseEntity<?> createRoot(@RequestHeader("X-User-Id") UUID userId) {
        try {
            folderService.createRootFolder(userId);
            return ResponseEntity.accepted().build();
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/{elementId}")
    public ResponseEntity<?> updateElement(@RequestHeader("X-User-Id") UUID userId, @RequestHeader("X-Client-Type") String client, @RequestBody UpdateFileRequest request, @PathVariable UUID elementId) {
        boolean isFolder = elementService.isFolder(elementId);
        fileAccessService.checkAccessElement(userId, elementId, isFolder, AccessType.WRITE);
        if (isFolder) {
            FolderEntity folder = folderService.updateFolderMetadata(elementId, request.getName());
            commandService.sendUpdate(elementId, client, userId.toString(), folder.getLastModification(), folder.getParent());
        } else {
            FileEntity file = fileService.updateFile(elementId, request.getName());
            commandService.sendUpdate(elementId, client, userId.toString(), file.getLastModification(), file.getParent());
        }
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/{elementId}/move/{folderId}")
    public ResponseEntity<?> moveFolder(@RequestHeader("X-User-Id") UUID userId, @RequestHeader("X-Client-Type") String client, @PathVariable UUID elementId, @PathVariable UUID folderId) {
        boolean isFolder = elementService.isFolder(elementId);
        fileAccessService.checkAccessElement(userId, elementId, isFolder, AccessType.WRITE);
        fileAccessService.checkAccessElement(userId, folderId, true, AccessType.WRITE);

        if (isFolder) {
            folderService.moveFile(elementId, folderId, client, userId);
        } else {
            fileService.moveFile(elementId, folderId, client, userId);
        }

        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{elementId}")
    public ResponseEntity<?> deleteElemnt(@RequestHeader("X-User-Id") UUID userId, @RequestHeader("X-Client-Type") String client, @PathVariable UUID elementId) {
        boolean isFolder = elementService.isFolder(elementId);
        fileAccessService.checkAccessElement(userId, elementId, isFolder, AccessType.WRITE);

        if (isFolder) {
            folderService.setRemoveFolder(elementId, client, userId);
        } else {
            fileService.setDeleteFile(elementId, client, userId);
        }

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