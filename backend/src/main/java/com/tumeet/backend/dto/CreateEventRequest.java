package com.tumeet.backend.dto;

public class CreateEventRequest {

    private String eventName;
    private String eventType;
    private String location;
    private String eventLocation;
    private Integer eventSize;
    private String eventDate;
    private String eventTime;
    private String creatorStudentId;

    public CreateEventRequest() {
    }

    public CreateEventRequest(String eventName, String eventType, Integer eventSize,
                              String eventDate, String eventTime, String creatorStudentId) {
        this.eventName = eventName;
        this.eventType = eventType;
        this.eventSize = eventSize;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.creatorStudentId = creatorStudentId;
    }

    public CreateEventRequest(String eventName, String eventType, String location,
                              Integer eventSize, String eventDate, String eventTime,
                              String creatorStudentId) {
        this.eventName = eventName;
        this.eventType = eventType;
        this.location = location;
        this.eventLocation = location;
        this.eventSize = eventSize;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.creatorStudentId = creatorStudentId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getLocation() {
        return location != null ? location : eventLocation;
    }

    public void setLocation(String location) {
        this.location = location;
        if (this.eventLocation == null) {
            this.eventLocation = location;
        }
    }

    public String getEventLocation() {
        return eventLocation != null ? eventLocation : location;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
        if (this.location == null) {
            this.location = eventLocation;
        }
    }

    public Integer getEventSize() {
        return eventSize;
    }

    public void setEventSize(Integer eventSize) {
        this.eventSize = eventSize;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getCreatorStudentId() {
        return creatorStudentId;
    }

    public void setCreatorStudentId(String creatorStudentId) {
        this.creatorStudentId = creatorStudentId;
    }
}
