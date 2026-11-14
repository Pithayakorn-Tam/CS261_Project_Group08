package com.tumeet.backend.controller;

import com.tumeet.backend.dto.ActionResponse;
import com.tumeet.backend.dto.ChatMessageDto;
import com.tumeet.backend.dto.SendChatMessageRequest;
import com.tumeet.backend.service.ChatService;
import com.tumeet.backend.websocket.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final ChatWebSocketHandler chatWebSocketHandler;

    public ChatController(ChatService chatService) {
        this(chatService, null);
    }

    @Autowired
    public ChatController(ChatService chatService, ChatWebSocketHandler chatWebSocketHandler) {
        this.chatService = chatService;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @GetMapping("/rooms")
    public ResponseEntity<ActionResponse> getChatRooms(@RequestParam(value = "studentId", required = false) String studentId) {
        ActionResponse response = chatService.getChatRoomsForUser(studentId);
        return response.isStatus()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/events/{eventId}/messages")
    public ResponseEntity<ActionResponse> getEventMessages(
            @PathVariable Long eventId,
            @RequestParam(value = "studentId", required = false) String studentId) {
        ActionResponse response = chatService.getMessagesByEvent(eventId, studentId);
        return response.isStatus()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/events/{eventId}/messages")
    public ResponseEntity<ActionResponse> sendEventMessage(
            @PathVariable Long eventId,
            @RequestBody SendChatMessageRequest request) {
        String senderStudentId = request != null ? request.getSenderStudentId() : null;
        String text = request != null ? request.getText() : null;
        ActionResponse response = chatService.sendMessage(eventId, senderStudentId, text);
        if (response.isStatus()) {
            if (chatWebSocketHandler != null && response.getData() instanceof ChatMessageDto dto) {
                chatWebSocketHandler.broadcastToRoom(String.valueOf(eventId), dto);
            }
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
}
