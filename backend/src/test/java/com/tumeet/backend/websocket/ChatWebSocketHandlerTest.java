package com.tumeet.backend.websocket;

import com.tumeet.backend.dto.ActionResponse;
import com.tumeet.backend.dto.ChatMessageDto;
import com.tumeet.backend.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketHandlerTest {

    @Mock
    private ChatService chatService;

    private ChatWebSocketHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ChatWebSocketHandler(chatService);
    }

    @Test
    @DisplayName("เข้าร่วมห้องแชทและ Broadcast ข้อความไปยังสมาชิกทุกคนในห้องเดียวกัน")
    void joinAndBroadcastMessage_Success() throws Exception {
        WebSocketSession session1 = mock(WebSocketSession.class);
        when(session1.getId()).thenReturn("sess-1");
        when(session1.isOpen()).thenReturn(true);

        WebSocketSession session2 = mock(WebSocketSession.class);
        when(session2.getId()).thenReturn("sess-2");
        when(session2.isOpen()).thenReturn(true);

        handler.handleTextMessage(session1, new TextMessage("{\"type\":\"join\",\"roomId\":\"10\",\"userId\":\"6800000000\"}"));
        handler.handleTextMessage(session2, new TextMessage("{\"type\":\"join\",\"roomId\":\"10\",\"userId\":\"6800000002\"}"));

        ChatMessageDto savedDto = new ChatMessageDto();
        savedDto.setId(99L);
        savedDto.setEventId(10L);
        savedDto.setRoomId("10");
        savedDto.setSenderStudentId("6800000000");
        savedDto.setSenderId("6800000000");
        savedDto.setSenderName("หมูกรอบ");
        savedDto.setText("สวัสดีทุกคน");
        savedDto.setTimestamp(LocalDateTime.of(2026, 11, 14, 13, 25));

        when(chatService.sendMessage(10L, "6800000000", "สวัสดีทุกคน"))
                .thenReturn(new ActionResponse(true, "ส่งข้อความสำเร็จ", savedDto));

        handler.handleTextMessage(session1, new TextMessage("{\"type\":\"message\",\"roomId\":\"10\",\"senderId\":\"6800000000\",\"text\":\"สวัสดีทุกคน\"}"));

        ArgumentCaptor<TextMessage> captor1 = ArgumentCaptor.forClass(TextMessage.class);
        ArgumentCaptor<TextMessage> captor2 = ArgumentCaptor.forClass(TextMessage.class);
        verify(session1).sendMessage(captor1.capture());
        verify(session2).sendMessage(captor2.capture());

        String sentJson = captor1.getValue().getPayload();
        assertTrue(sentJson.contains("\"type\":\"message\""));
        assertTrue(sentJson.contains("\"eventId\":10"));
        assertTrue(sentJson.contains("\"senderStudentId\":\"6800000000\""));
        assertTrue(sentJson.contains("\"text\":\"สวัสดีทุกคน\""));
        assertEquals(sentJson, captor2.getValue().getPayload());
    }

    @Test
    @DisplayName("ไม่ Broadcast เมื่อส่งข้อความไม่สำเร็จและจัดการตัดการเชื่อมต่อ Session ได้")
    void failedSendAndConnectionClose_HandledCleanly() throws Exception {
        WebSocketSession session1 = mock(WebSocketSession.class);
        when(session1.getId()).thenReturn("sess-1");

        handler.handleTextMessage(session1, new TextMessage("{\"type\":\"join\",\"roomId\":\"room10\",\"userId\":\"6800000009\"}"));

        when(chatService.sendMessage(10L, "6800000009", "test"))
                .thenReturn(new ActionResponse(false, "คุณไม่มีสิทธิ์เข้าถึงห้องแชทของกิจกรรมนี้"));

        handler.handleTextMessage(session1, new TextMessage("{\"type\":\"message\",\"roomId\":\"room10\",\"senderId\":\"6800000009\",\"text\":\"test\"}"));
        verify(session1, never()).sendMessage(any());

        // Invalid JSON does not throw
        assertDoesNotThrow(() -> handler.handleTextMessage(session1, new TextMessage("not-json")));

        handler.afterConnectionClosed(session1, CloseStatus.NORMAL);

        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(1L);
        dto.setEventId(10L);
        dto.setText("hi");
        handler.broadcastToRoom("10", dto);
        verify(session1, never()).sendMessage(any());
    }
}
