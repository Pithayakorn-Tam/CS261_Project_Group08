package com.tumeet.backend.dto;

import com.tumeet.backend.model.ChatMessage;
import com.tumeet.backend.model.User;

import java.time.LocalDateTime;

public class ChatMessageDto {

    private Long id;
    private String type = "message";
    private Long eventId;
    private String roomId;
    private String senderStudentId;
    private String senderId;
    private String senderName;
    private String senderAvatar;
    private String text;
    private LocalDateTime timestamp;

    public ChatMessageDto() {
    }

    public static ChatMessageDto fromEntity(ChatMessage message) {
        if (message == null) {
            return null;
        }
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(message.getId());
        dto.setType("message");

        Long resolvedEventId = message.getEvent() != null ? message.getEvent().getId() : null;
        dto.setEventId(resolvedEventId);
        dto.setRoomId(resolvedEventId != null ? String.valueOf(resolvedEventId) : null);

        User sender = message.getSender();
        if (sender != null) {
            String studentId = sender.getStudentId();
            dto.setSenderStudentId(studentId);
            dto.setSenderId(studentId);
            dto.setSenderName(resolveSenderName(sender));
        }
        dto.setSenderAvatar(null);
        dto.setText(message.getMessageText());
        dto.setTimestamp(message.getSentAt() != null ? message.getSentAt() : LocalDateTime.now());
        return dto;
    }

    private static String resolveSenderName(User sender) {
        if (sender.getUsername() != null && !sender.getUsername().isBlank()) {
            return sender.getUsername().trim();
        }
        if (sender.getDisplayname_th() != null && !sender.getDisplayname_th().isBlank()) {
            return sender.getDisplayname_th().trim();
        }
        if (sender.getDisplayname_en() != null && !sender.getDisplayname_en().isBlank()) {
            return sender.getDisplayname_en().trim();
        }
        return sender.getStudentId();
    }

    /**
     * Serializes this DTO directly to JSON for WebSocket broadcast without coupling
     * to a specific Jackson major version.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        appendJsonStringField(sb, "type", type != null ? type : "message", true);
        sb.append(',');
        appendJsonNumberField(sb, "id", id);
        sb.append(',');
        appendJsonNumberField(sb, "eventId", eventId);
        sb.append(',');
        appendJsonStringField(sb, "roomId", roomId, true);
        sb.append(',');
        appendJsonStringField(sb, "senderStudentId", senderStudentId, true);
        sb.append(',');
        appendJsonStringField(sb, "senderId", senderId, true);
        sb.append(',');
        appendJsonStringField(sb, "senderName", senderName, true);
        sb.append(',');
        appendJsonStringField(sb, "senderAvatar", senderAvatar, true);
        sb.append(',');
        appendJsonStringField(sb, "text", text, true);
        sb.append(',');
        appendJsonStringField(sb, "timestamp", timestamp != null ? timestamp.toString() : null, true);
        sb.append('}');
        return sb.toString();
    }

    private static void appendJsonNumberField(StringBuilder sb, String name, Long value) {
        sb.append('"').append(escapeJson(name)).append("\":");
        sb.append(value != null ? value : "null");
    }

    private static void appendJsonStringField(StringBuilder sb, String name, String value, boolean nullable) {
        sb.append('"').append(escapeJson(name)).append("\":");
        if (value == null) {
            sb.append(nullable ? "null" : "\"\"");
        } else {
            sb.append('"').append(escapeJson(value)).append('"');
        }
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (c < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
                }
            }
        }
        return escaped.toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getSenderStudentId() {
        return senderStudentId;
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

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
