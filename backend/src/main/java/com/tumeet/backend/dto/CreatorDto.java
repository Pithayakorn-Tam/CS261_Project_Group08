package com.tumeet.backend.dto;

import com.tumeet.backend.model.User;

public class CreatorDto {

    private String studentId;
    private String displayname_th;
    private String faculty;
    private String email;

    public CreatorDto() {
    }

    public CreatorDto(String studentId, String displayname_th, String faculty, String email) {
        this.studentId = studentId;
        this.displayname_th = displayname_th;
        this.faculty = faculty;
        this.email = email;
    }

    public static CreatorDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new CreatorDto(
                user.getStudentId(),
                user.getDisplayname_th(),
                user.getFaculty(),
                user.getEmail()
        );
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getDisplayname_th() {
        return displayname_th;
    }

    public void setDisplayname_th(String displayname_th) {
        this.displayname_th = displayname_th;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
