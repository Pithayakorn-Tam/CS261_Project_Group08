package com.tumeet.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest {

    @JsonAlias({"UserName", "username"})
    private String studentId;

    @JsonAlias({"PassWord", "password", "citizenId", "IdNum"})
    private String idNum;

    private String email;

    public LoginRequest() {
    }

    public LoginRequest(String studentId, String idNum) {
        this.studentId = studentId;
        this.idNum = idNum;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getIdNum() {
        return idNum;
    }

    public void setIdNum(String idNum) {
        this.idNum = idNum;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
