package edu.udg.tfg.FileManagement.controlllers.requests;

import java.util.UUID;

public class UpdateFileRequest {
    private String name;

    public UpdateFileRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}