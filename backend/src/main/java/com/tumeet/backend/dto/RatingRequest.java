package com.tumeet.backend.dto;

public class RatingRequest {
    private String raterStudentId;
    private String rateeStudentId;
    private Integer score;
    private Integer q1Score;
    private Integer q2Score;
    private Integer q3Score;
    private Integer q4Score;
    private String comment;
    private String role;

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

    public Integer getQ1Score() {
        return q1Score;
    }

    public void setQ1Score(Integer q1Score) {
        this.q1Score = q1Score;
    }

    public Integer getQ2Score() {
        return q2Score;
    }

    public void setQ2Score(Integer q2Score) {
        this.q2Score = q2Score;
    }

    public Integer getQ3Score() {
        return q3Score;
    }

    public void setQ3Score(Integer q3Score) {
        this.q3Score = q3Score;
    }

    public Integer getQ4Score() {
        return q4Score;
    }

    public void setQ4Score(Integer q4Score) {
        this.q4Score = q4Score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
