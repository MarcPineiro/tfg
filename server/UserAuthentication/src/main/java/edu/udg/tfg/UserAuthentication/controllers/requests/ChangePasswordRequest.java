package edu.udg.tfg.UserAuthentication.controllers.requests;

public class ChangePasswordRequest {
    private String password;

    public ChangePasswordRequest() {}

    public ChangePasswordRequest(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
