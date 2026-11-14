package com.tumeet.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    @Column(name = "tu_status", length = 50)
    private String tu_status;

    @Column(name = "statusid", length = 20)
    private String statusid;

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

    // --- [US-9] จำนวนกิจกรรมที่สร้างสำเร็จและเข้าร่วมสำเร็จ ---
    @Column(name = "completed_created_count", nullable = false)
    @ColumnDefault("0")
    private Integer completedCreatedCount = 0;

    @Column(name = "completed_joined_count", nullable = false)
    @ColumnDefault("0")
    private Integer completedJoinedCount = 0;

    public User() {
    }

    public User(String studentId, String email, String type, String tu_status, String statusid,
                String displayname_th, String displayname_en, String faculty, String department) {
        this.studentId = studentId;
        this.email = email;
        this.type = type;
        this.tu_status = tu_status;
        this.statusid = statusid;
        this.statusname = tu_status;
        this.displayname_th = displayname_th;
        this.displayname_en = displayname_en;
        this.faculty = faculty;
        this.department = department;
    }

    public User(String studentId, String email, String type, String tu_status, String statusid,
                String statusname, String prefixname, String displayname_th, String displayname_en,
                String faculty, String department) {
        this.studentId = studentId;
        this.email = email;
        this.type = type;
        this.tu_status = tu_status;
        this.statusid = statusid;
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

    public String getTu_status() {
        return tu_status;
    }

    public void setTu_status(String tu_status) {
        this.tu_status = tu_status;
    }

    public String getStatusid() {
        return statusid;
    }

    public void setStatusid(String statusid) {
        this.statusid = statusid;
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

    public Integer getCompletedCreatedCount() {
        return completedCreatedCount;
    }

    public void setCompletedCreatedCount(Integer completedCreatedCount) {
        this.completedCreatedCount = completedCreatedCount != null ? completedCreatedCount : 0;
    }

    public Integer getCompletedJoinedCount() {
        return completedJoinedCount;
    }

    public void setCompletedJoinedCount(Integer completedJoinedCount) {
        this.completedJoinedCount = completedJoinedCount != null ? completedJoinedCount : 0;
    }

    // --- [US-9] เมธอดคำนวณยศใน Java (ไม่มีคอลัมน์ badge ใน DB แต่ Jackson จะ serialize ออกไปใน JSON ชื่อ "badge" อัตโนมัติ) ---
    public String getBadge() {
        int created = this.completedCreatedCount != null ? this.completedCreatedCount : 0;
        int joined = this.completedJoinedCount != null ? this.completedJoinedCount : 0;
        int total = created + joined;

        if (total >= 20) return "Super Bouquet";
        if (total >= 10) return "Happy Blossom";
        if (total >= 5)  return "Baby Bloom";
        if (total >= 1)  return "Newly Bee";
        return "No Badge";
    }
}
