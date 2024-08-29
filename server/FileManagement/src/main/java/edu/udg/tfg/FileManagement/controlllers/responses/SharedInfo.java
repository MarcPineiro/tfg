package edu.udg.tfg.FileManagement.controlllers.responses;

public class SharedInfo {
    private String userId;
    private String username;
    private int accessLevel;

    public SharedInfo(String userId, String username, int accessLevel) {
        this.userId = userId;
        this.username = username;
        this.accessLevel = accessLevel;
    }

    public SharedInfo() {

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }
}