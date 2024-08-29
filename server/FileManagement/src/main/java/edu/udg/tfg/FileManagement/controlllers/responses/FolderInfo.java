package edu.udg.tfg.FileManagement.controlllers.responses;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class FolderInfo {
    private UUID id;
    private String name;
    private Date creationDate;
    private Date lastModification;
    private List<FileInfo> subfolders;
    private List<FileInfo> files;
    private String owner;
    private List<SharedInfo> sharedWith;

    public FolderInfo() {
    }

    public FolderInfo(UUID id, String name, Date creationDate, Date lastModification, List<FileInfo> subfolders, List<FileInfo> files, String owner, List<SharedInfo> sharedWith) {
        this.id = id;
        this.name = name;
        this.creationDate = creationDate;
        this.lastModification = lastModification;
        this.subfolders = subfolders;
        this.files = files;
        this.owner = owner;
        this.sharedWith = sharedWith;
    }

    public FolderInfo(UUID id, String name, Date creationDate, Date lastModification, String owner) {
        this.id = id;
        this.name = name;
        this.creationDate = creationDate;
        this.lastModification = lastModification;
        this.owner = owner;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModification() {
        return lastModification;
    }

    public void setLastModification(Date lastModification) {
        this.lastModification = lastModification;
    }

    public List<FileInfo> getSubfolders() {
        return subfolders;
    }

    public void setSubfolders(List<FileInfo> subfolders) {
        this.subfolders = subfolders;
    }

    public List<FileInfo> getFiles() {
        return files;
    }

    public void setFiles(List<FileInfo> files) {
        this.files = files;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<SharedInfo> getSharedWith() {
        return sharedWith;
    }

    public void setSharedWith(List<SharedInfo> sharedWith) {
        this.sharedWith = sharedWith;
    }
}
