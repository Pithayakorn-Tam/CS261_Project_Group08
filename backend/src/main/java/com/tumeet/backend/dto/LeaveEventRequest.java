package com.tumeet.backend.dto;

public class LeaveEventRequest {
    private Long eventId;
    private String studentId;

    public LeaveEventRequest() {}

    public LeaveEventRequest(Long eventId, String studentId) {
        this.eventId = eventId;
        this.studentId = studentId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
}