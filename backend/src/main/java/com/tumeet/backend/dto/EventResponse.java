package com.tumeet.backend.dto;

public class EventResponse {

    private boolean status;
    private String message;
    private Object data;

    public EventResponse() {
    }

    public EventResponse(boolean status, String message) {
        this.status = status;
        this.message = message;
        this.data = null;
    }

    public EventResponse(boolean status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static EventResponse success(String message, Object data) {
        return new EventResponse(true, message, data);
    }

    public static EventResponse success(String message) {
        return new EventResponse(true, message, null);
    }

    public static EventResponse error(String message) {
        return new EventResponse(false, message, null);
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
