package edu.udg.tfg.FileManagement.services;

import edu.udg.tfg.FileManagement.entities.ElementEntity;
import edu.udg.tfg.FileManagement.entities.FileEntity;
import edu.udg.tfg.FileManagement.entities.FolderEntity;
import edu.udg.tfg.FileManagement.feignClients.fileAccess.*;
import edu.udg.tfg.FileManagement.queue.Sender;
import edu.udg.tfg.FileManagement.repositories.FileRepository;
import edu.udg.tfg.FileManagement.repositories.FolderRepository;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FileAccessService {

    @Autowired
    private FileAccessClient fileAccessClient;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private Sender sender;

    private boolean hasRequiredAccess(AccessResponse accessResponse, AccessType type) {
        return type.ordinal() <= accessResponse.getAccessType();
    }

    public void deleteFileAccess(UUID fileId, UUID userId) {
        fileAccessClient.deleteFileAccess(fileId.toString(), userId.toString());
    }

    public List<AccessResponse> getFileAccessByUserId(UUID userId) {
        return fileAccessClient.getFileAccessByUserId(userId.toString());
    }

    public List<AccessResponse> getFileAccessByFileId(UUID fileId) {
        return fileAccessClient.getFileAccessByFileId(fileId.toString());
    }

    public boolean checkAccessFile(UUID userId, UUID fileId, AccessType method) {
        if (fileId == null) {
            return false;
        }
        FileEntity fileEntity = fileRepository.findByElementId(new ElementEntity(fileId)).orElseThrow(() ->new NotFoundException("File not found"));
        AccessResponse accessResponse = fileAccessClient.getFileAccess(fileEntity.getElementId().toString(), userId.toString());
        return accessResponse != null && hasRequiredAccess(accessResponse, method);
    }

    public boolean checkAccessFolder(UUID userId, UUID fileId, AccessType method) {
        if (fileId == null) {
            return false;
        }
        FolderEntity fileEntity = folderRepository.findByElementId(new ElementEntity(fileId)).orElseThrow(() ->new NotFoundException("Folder not found"));
        AccessResponse accessResponse = fileAccessClient.getFileAccess(fileEntity.getElementId().toString(), userId.toString());
        return accessResponse != null && hasRequiredAccess(accessResponse, method);
    }

    public boolean checkAccessFolder(FolderEntity folderEntity, UUID userId, AccessType accessType) {
        if(!checkAccessFolder(folderEntity.getElementId(), userId, accessType)) {
            return false;
        }
        for(FolderEntity child : folderEntity.getChildren()) {
            return checkAccessFolder(child, userId, accessType);
        }
        for(FileEntity file : folderEntity.getFiles()) {
            if(!checkAccessFile(file.getElementId(), userId, accessType)) {
                return false;
            }
        }
        return true;
    }

    public void addFileAccess(UUID elementId, UUID userId, AccessType accessType) {
        AccessRequest accessRequest = new AccessRequest();
        accessRequest.setAccessType(accessType.ordinal());
        accessRequest.setElementId(elementId);
        accessRequest.setUserId(userId);
        fileAccessClient.addFileAccess(accessRequest);
    }

    public void deleteFileAccessAsincornous(UUID fileId, UUID userId) {
        sender.deleteAccess(fileId, userId);
    }

    public void addFileAccessAssincronous(UUID elementId, UUID userId, AccessType accessType) {
        sender.addAccess(elementId, userId, accessType);
    }

    public void checkAccessElement(UUID userId, UUID elementId, boolean isFolder, AccessType type) {
        if (isFolder) {
            if (!checkAccessFolder(userId,elementId, type)) {
                throw new ForbiddenException();
            }
        } else {
            if (!checkAccessFile(userId, elementId, type)) {
                throw new ForbiddenException();
            }
        }
    }

    public int getFileAccessLevelByFileIdAndUserId(UUID elementId, UUID id) {
        AccessResponse accessResponse = fileAccessClient.getFileAccess(elementId.toString(), id.toString());
        return accessResponse.getAccessType();
    }
}

