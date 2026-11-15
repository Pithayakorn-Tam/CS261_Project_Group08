package com.tumeet.backend.dto;

public class RatingMemberDto {
    private String studentId;
    private String username;
    private String displayname_th;
    private String profileImage;
    private boolean alreadyRated;
    private Integer givenScore; // 1-5 หรือ null ถ้ายังไม่ได้ประเมิน

    public RatingMemberDto() {}

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

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public boolean isAlreadyRated() {
        return alreadyRated;
    }

    public void setAlreadyRated(boolean alreadyRated) {
        this.alreadyRated = alreadyRated;
    }

    public Integer getGivenScore() {
        return givenScore;
    }

    public void setGivenScore(Integer givenScore) {
        this.givenScore = givenScore;
    }
}
