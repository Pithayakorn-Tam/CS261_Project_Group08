package com.tumeet.backend.websocket;

import com.tumeet.backend.dto.ActionResponse;
import com.tumeet.backend.dto.ChatMessageDto;
import com.tumeet.backend.service.ChatService;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final JsonParser jsonParser = JsonParserFactory.getJsonParser();
    private final Map<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionRooms = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if (session == null || message == null) {
            return;
        }

        Map<String, Object> payload;
        try {
            payload = jsonParser.parseMap(message.getPayload());
        } catch (Exception e) {
            return;
        }

        String type = asString(payload.get("type"));
        String roomId = asString(payload.get("roomId"));
        if (roomId == null || roomId.isBlank()) {
            return;
        }
        roomId = roomId.trim();

        if ("join".equalsIgnoreCase(type)) {
            joinRoom(session, roomId);
            return;
        }

        if ("message".equalsIgnoreCase(type)) {
            Long eventId = resolveEventId(roomId);
            if (eventId == null) {
                return;
            }

            String senderId = asString(payload.get("senderId"));
            if (senderId == null || senderId.isBlank()) {
                senderId = asString(payload.get("senderStudentId"));
            }
            if (senderId == null || senderId.isBlank()) {
                senderId = asString(payload.get("userId"));
            }

            String text = asString(payload.get("text"));
            ActionResponse response = chatService.sendMessage(eventId, senderId, text);
            if (response != null && response.isStatus() && response.getData() instanceof ChatMessageDto dto) {
                dto.setRoomId(roomId);
                broadcastToEventRooms(roomId, eventId, dto);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        if (session == null) {
            return;
        }
        String previousRoom = sessionRooms.remove(session.getId());
        if (previousRoom != null) {
            removeSessionFromRoom(previousRoom, session);
        }
    }

    public void broadcastToRoom(String roomId, ChatMessageDto dto) {
        if (roomId == null || roomId.isBlank() || dto == null) {
            return;
        }
        Long eventId = dto.getEventId() != null ? dto.getEventId() : resolveEventId(roomId);
        broadcastToEventRooms(roomId.trim(), eventId, dto);
    }

    private void joinRoom(WebSocketSession session, String roomId) {
        String previousRoom = sessionRooms.put(session.getId(), roomId);
        if (previousRoom != null && !previousRoom.equals(roomId)) {
            removeSessionFromRoom(previousRoom, session);
        }
        roomSessions.computeIfAbsent(roomId, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    private void removeSessionFromRoom(String roomId, WebSocketSession session) {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(roomId, sessions);
            }
        }
    }

    private void broadcastToEventRooms(String roomId, Long eventId, ChatMessageDto dto) {
        String json = dto.toJson();
        Set<String> targetRooms = new HashSet<>();
        targetRooms.add(roomId);
        if (eventId != null) {
            targetRooms.add(String.valueOf(eventId));
            targetRooms.add("room" + eventId);
        }

        Set<String> deliveredSessionIds = new HashSet<>();
        for (String targetRoom : targetRooms) {
            Set<WebSocketSession> sessions = roomSessions.get(targetRoom);
            if (sessions == null || sessions.isEmpty()) {
                continue;
            }
            for (WebSocketSession targetSession : Set.copyOf(sessions)) {
                if (targetSession == null || !targetSession.isOpen()) {
                    removeSessionFromRoom(targetRoom, targetSession);
                    continue;
                }
                if (!deliveredSessionIds.add(targetSession.getId())) {
                    continue;
                }
                try {
                    synchronized (targetSession) {
                        targetSession.sendMessage(new TextMessage(json));
                    }
                } catch (IOException e) {
                    removeSessionFromRoom(targetRoom, targetSession);
                }
            }
        }
    }

    Long resolveEventId(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            return null;
        }
        String normalized = roomId.trim();
        if (normalized.regionMatches(true, 0, "room", 0, 4)) {
            normalized = normalized.substring(4).trim();
        }
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String asString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }
}
