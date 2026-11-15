package com.tumeet.backend.dto;

public class RatingRequest {
    private String raterStudentId;
    private String rateeStudentId;
    private Integer score;

    public RatingRequest() {}

    public RatingRequest(String raterStudentId, String rateeStudentId, Integer score) {
        this.raterStudentId = raterStudentId;
        this.rateeStudentId = rateeStudentId;
        this.score = score;
    }

    public String getRaterStudentId() {
        return raterStudentId;
    }

    public void setRaterStudentId(String raterStudentId) {
        this.raterStudentId = raterStudentId;
    }

    public String getRateeStudentId() {
        return rateeStudentId;
    }

    public void setRateeStudentId(String rateeStudentId) {
        this.rateeStudentId = rateeStudentId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
