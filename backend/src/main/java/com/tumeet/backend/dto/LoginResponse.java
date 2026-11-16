package com.tumeet.backend.dto;

import com.tumeet.backend.model.User;

public class LoginResponse {

    private boolean status;
    private String message;
    private String studentId;
    private String email;
    private String username;
    private String displayname_th;
    private String displayname_en;
    private String faculty;
    private String department;

    public LoginResponse() {
    }

    public LoginResponse(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public static LoginResponse success() {
        return new LoginResponse(true, "Login Success");
    }

    public static LoginResponse success(User user) {
        LoginResponse res = new LoginResponse(true, "Login Success");
        if (user != null) {
            res.setStudentId(user.getStudentId());
            res.setEmail(user.getEmail());
            res.setUsername(user.getDisplayname_th() != null ? user.getDisplayname_th() : user.getStudentId());
            res.setDisplayname_th(user.getDisplayname_th());
            res.setDisplayname_en(user.getDisplayname_en());
            res.setFaculty(user.getFaculty());
            res.setDepartment(user.getDepartment());
        }
        return res;
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

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayname_th() {
        return displayname_th;
    }

    public void setDisplayname_th(String displayname_th) {
        this.displayname_th = displayname_th;
    }

    public String getDisplayname_en() {
        return displayname_en;
    }

    public void setDisplayname_en(String displayname_en) {
        this.displayname_en = displayname_en;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
