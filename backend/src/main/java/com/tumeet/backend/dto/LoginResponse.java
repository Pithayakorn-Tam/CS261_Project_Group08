package com.tumeet.backend.dto;

public class LoginResponse {

    private boolean status;
    private String message;

    public LoginResponse() {
    }

    public LoginResponse(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public static LoginResponse success() {
        return new LoginResponse(true, "Login Success");
    }

    public static LoginResponse error(String message) {
        return new LoginResponse(false, message);
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
