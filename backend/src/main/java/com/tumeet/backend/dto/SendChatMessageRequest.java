package com.tumeet.backend.dto;

public class SendChatMessageRequest {

    private String senderStudentId;
    private String senderId;
    private String text;
    private String messageText;

    public SendChatMessageRequest() {
    }

    public SendChatMessageRequest(String senderStudentId, String text) {
        this.senderStudentId = senderStudentId;
        this.text = text;
    }

    public String getSenderStudentId() {
        return (senderStudentId != null && !senderStudentId.isBlank()) ? senderStudentId : senderId;
    }

    public void setSenderStudentId(String senderStudentId) {
        this.senderStudentId = senderStudentId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getText() {
        return text != null ? text : messageText;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}
