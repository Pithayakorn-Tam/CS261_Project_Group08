package com.tumeet.backend.dto;

import com.tumeet.backend.model.User;

public class CreatorDto {

    private String studentId;
    private String username;
    private String displayname_th;
    private String faculty;
    private String email;
    private Integer completedCreatedCount;
    private Integer completedJoinedCount;
    private String badge;

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
        CreatorDto dto = new CreatorDto(
                user.getStudentId(),
                user.getDisplayname_th(),
                user.getFaculty(),
                user.getEmail()
        );
        dto.setUsername(user.getUsername());
        dto.setCompletedCreatedCount(user.getCompletedCreatedCount() != null ? user.getCompletedCreatedCount() : 0);
        dto.setCompletedJoinedCount(user.getCompletedJoinedCount() != null ? user.getCompletedJoinedCount() : 0);
        dto.setBadge(user.getBadge());
        return dto;
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

    public Integer getCompletedCreatedCount() {
        return completedCreatedCount;
    }

    public void setCompletedCreatedCount(Integer completedCreatedCount) {
        this.completedCreatedCount = completedCreatedCount;
    }

    public Integer getCompletedJoinedCount() {
        return completedJoinedCount;
    }

    public void setCompletedJoinedCount(Integer completedJoinedCount) {
        this.completedJoinedCount = completedJoinedCount;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }
}
