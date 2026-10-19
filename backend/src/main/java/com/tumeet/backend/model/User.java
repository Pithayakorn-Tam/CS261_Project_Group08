package com.tumeet.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false, unique = true, length = 20)
    private String studentId;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 50)
    private String type;

    @Column(length = 50)
    private String statusname;

    @Column(length = 20)
    private String prefixname;

    @Column(name = "displayname_th", length = 150)
    private String displayname_th;

    @Column(name = "displayname_en", length = 150)
    private String displayname_en;

    @Column(length = 150)
    private String faculty;

    @Column(length = 150)
    private String department;

    @Column(length = 100)
    private String username;

    public User() {
    }

    public User(String studentId, String email, String type, String statusname, String prefixname,
                String displayname_th, String displayname_en, String faculty, String department) {
        this.studentId = studentId;
        this.email = email;
        this.type = type;
        this.statusname = statusname;
        this.prefixname = prefixname;
        this.displayname_th = displayname_th;
        this.displayname_en = displayname_en;
        this.faculty = faculty;
        this.department = department;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatusname() {
        return statusname;
    }

    public void setStatusname(String statusname) {
        this.statusname = statusname;
    }

    public String getPrefixname() {
        return prefixname;
    }

    public void setPrefixname(String prefixname) {
        this.prefixname = prefixname;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
