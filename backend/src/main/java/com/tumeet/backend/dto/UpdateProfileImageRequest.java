package com.tumeet.backend.dto;

public class UpdateProfileImageRequest {
    private String studentId;
    private String profileImage; // Base64 Data URL

    public UpdateProfileImageRequest() {}

    public UpdateProfileImageRequest(String studentId, String profileImage) {
        this.studentId = studentId;
        this.profileImage = profileImage;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
