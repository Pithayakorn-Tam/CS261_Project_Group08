package com.tumeet.backend.dto;

public class UpdateUsernameRequest {
    private String studentId;
    private String username;

    public UpdateUsernameRequest() {}

    public UpdateUsernameRequest(String studentId, String username) {
        this.studentId = studentId;
        this.username = username;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
