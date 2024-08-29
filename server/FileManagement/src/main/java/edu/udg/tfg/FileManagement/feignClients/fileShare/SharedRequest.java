package edu.udg.tfg.FileManagement.feignClients.fileShare;

import java.util.List;
import java.util.UUID;

public class SharedRequest {
    private UUID elementId;
    //private FolderId folderId;
    private List<UUID> files;
    private boolean root;

    public SharedRequest() {}

    public SharedRequest(UUID elementId, List<UUID> files, boolean root) {
        this.elementId = elementId;
        this.files = files;
        this.root = root;
    }

    public boolean isRoot() {
        return root;
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    public UUID getElementId() {
        return elementId;
    }

    public void setElementId(UUID elementId) {
        this.elementId = elementId;
    }

    public List<UUID> getFiles() {
        return files;
    }

    public void setFiles(List<UUID> files) {
        this.files = files;
    }
}
