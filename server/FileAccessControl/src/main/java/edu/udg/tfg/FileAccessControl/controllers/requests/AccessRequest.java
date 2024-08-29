package edu.udg.tfg.FileAccessControl.controllers.requests;

import edu.udg.tfg.FileAccessControl.entities.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.List;
import java.util.UUID;

public class AccessRequest {
    private UUID userId;
    private UUID elementId;
    private int accessType;
    /*private FolderId folderId;
    private List<UUID> files;*/

    /*public FolderId getFolderId() {
        return folderId;
    }

    public void setFolderId(FolderId folderId) {
        this.folderId = folderId;
    }

    public List<UUID> getFiles() {
        return files;
    }

    public void setFiles(List<UUID> files) {
        this.files = files;
    }*/

    public AccessRequest() {
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getElementId() {
        return elementId;
    }

    public void setElementId(UUID elementId) {
        this.elementId = elementId;
    }

    public int getAccessType() {
        return accessType;
    }

    public void setAccessType(int accessType) {
        this.accessType = accessType;
    }
}
