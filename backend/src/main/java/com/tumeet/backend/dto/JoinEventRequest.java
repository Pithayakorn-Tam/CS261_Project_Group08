package com.tumeet.backend.dto;

public class JoinEventRequest {
    private Long eventId;
    private String studentId;

    public JoinEventRequest() {}

    public JoinEventRequest(Long eventId, String studentId) {
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
