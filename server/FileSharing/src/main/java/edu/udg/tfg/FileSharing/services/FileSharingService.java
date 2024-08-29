package edu.udg.tfg.FileSharing.services;

import edu.udg.tfg.FileSharing.controllers.requests.FolderId;
import edu.udg.tfg.FileSharing.controllers.requests.SharedRequest;
import edu.udg.tfg.FileSharing.entities.SharedAccess;
import edu.udg.tfg.FileSharing.repositories.SharedAccessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileSharingService {

    @Autowired
    SharedAccessRepository sharedAccessRepository;

    public void shareFile(UUID fileId, UUID userId, boolean root) {
        SharedAccess sharedAccess = new SharedAccess();
        sharedAccess.setElementId(fileId);
        sharedAccess.setUserId(userId);
        sharedAccess.setRoot(root);
        sharedAccessRepository.save(sharedAccess);
    }

    public List<SharedAccess> getSharedFiles(UUID userId) {
        return sharedAccessRepository.findByUserIdAndRoot(userId, true);
    }

    public void revokeSharedFile(SharedRequest sharedRequest, UUID userId) {
        deleteChildren(sharedRequest.getFiles(), userId);
        sharedAccessRepository.deleteByElementIdAndUserId(sharedRequest.getElementId(), userId);
    }

    private void deleteChildren(List<UUID> files, UUID userId) {
        for (UUID fileId : files) {
            sharedAccessRepository.deleteByElementIdAndUserId(fileId, userId);
        }
    }

    public void shareFile(SharedRequest sharedRequest, UUID userId) {
        checkFilesChildren(sharedRequest.getFiles(), userId);
        shareFile(sharedRequest.getElementId(), userId, sharedRequest.isRoot());
    }

    private void checkFilesChildren(List<UUID> files, UUID userId) {
        for (UUID fileId : files) {
            Optional<SharedAccess> sharedAccess = sharedAccessRepository.findById(fileId);
            if (sharedAccess.isEmpty()) {
                shareFile(fileId, userId, false);
            }
        }
    }

    private Optional<SharedAccess> checkParentFolders(FolderId folderId, UUID userId) {
        if(folderId == null) {
            return Optional.empty();
        }
        Optional<SharedAccess> sharedAccess = sharedAccessRepository.findByElementIdAndUserId(folderId.getId(), userId);
        if(sharedAccess.isPresent()) {
            return sharedAccess;
        }
        else return checkParentFolders(folderId.getFolderId(), userId);
    }

    public void delete(UUID elementId, UUID userId) {
        sharedAccessRepository.deleteByElementIdAndUserId(elementId, userId);
    }

    public List<SharedAccess> getSharedUsers(UUID elementId) {
        return sharedAccessRepository.findByElementId(elementId);
    }
}