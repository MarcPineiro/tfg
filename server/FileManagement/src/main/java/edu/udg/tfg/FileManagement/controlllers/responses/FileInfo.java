package edu.udg.tfg.FileManagement.controlllers.responses;

import jakarta.persistence.Column;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class FileInfo {
    private UUID id;
    private String name;
    private Date creationDate;
    private Date lastModification;

    private String owner;  // New field
    private List<SharedInfo> sharedWith;

    public FileInfo(UUID id, String name, Date creationDate, Date lastModification, String owner, List<SharedInfo> sharedWith) {
        this.id = id;
        this.name = name;
        this.creationDate = creationDate;
        this.lastModification = lastModification;
        this.owner = owner;
        this.sharedWith = sharedWith;
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


}
