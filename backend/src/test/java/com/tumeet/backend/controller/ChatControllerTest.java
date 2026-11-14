package com.tumeet.backend.controller;

import com.tumeet.backend.dto.ActionResponse;
import com.tumeet.backend.dto.ChatMessageDto;
import com.tumeet.backend.dto.ChatRoomDto;
import com.tumeet.backend.service.ChatService;
import com.tumeet.backend.websocket.ChatWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ChatService chatService;

    @Mock
    private ChatWebSocketHandler chatWebSocketHandler;

    @BeforeEach
    void setUp() {
        ChatController controller = new ChatController(chatService, chatWebSocketHandler);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /api/chat/rooms คืนค่า 200 OK พร้อมรายการห้องแชท")
    void getChatRooms_ReturnsOk() throws Exception {
        ChatRoomDto room = new ChatRoomDto(10L, "หมูกระทะ", "food", 3, 6, "UPCOMING", "สวัสดี", LocalDateTime.now());
        when(chatService.getChatRoomsForUser("6800000000"))
                .thenReturn(new ActionResponse(true, "ดึงรายการห้องแชทสำเร็จ", List.of(room)));

        mockMvc.perform(get("/api/chat/rooms").param("studentId", "6800000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.data[0].eventId").value(10))
                .andExpect(jsonPath("$.data[0].eventName").value("หมูกระทะ"))
                .andExpect(jsonPath("$.data[0].lastMessage").value("สวัสดี"));
    }

    @Test
    @DisplayName("GET /api/chat/events/{eventId}/messages คืนค่า 200 OK และ 400 Bad Request เมื่อไม่มีสิทธิ์")
    void getEventMessages_ReturnsOkAndBadRequest() throws Exception {
        ChatMessageDto msg = new ChatMessageDto();
        msg.setId(1L);
        msg.setEventId(10L);
        msg.setSenderStudentId("6800000000");
        msg.setSenderName("หมูกรอบ");
        msg.setText("เจอกันหน้าตลาด");

        when(chatService.getMessagesByEvent(10L, "6800000000"))
                .thenReturn(new ActionResponse(true, "ดึงประวัติข้อความแชทสำเร็จ", List.of(msg)));
        when(chatService.getMessagesByEvent(10L, "6800000009"))
                .thenReturn(new ActionResponse(false, "คุณไม่มีสิทธิ์เข้าถึงห้องแชทของกิจกรรมนี้"));

        mockMvc.perform(get("/api/chat/events/10/messages").param("studentId", "6800000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.data[0].text").value("เจอกันหน้าตลาด"));

        mockMvc.perform(get("/api/chat/events/10/messages").param("studentId", "6800000009"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    @DisplayName("POST /api/chat/events/{eventId}/messages คืนค่า 200 OK และ Broadcast ไปยัง WebSocket Room")
    void sendEventMessage_SuccessAndBroadcasts() throws Exception {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(55L);
        dto.setEventId(10L);
        dto.setRoomId("10");
        dto.setSenderStudentId("6800000000");
        dto.setSenderName("หมูกรอบ");
        dto.setText("สวัสดีครับ");

        when(chatService.sendMessage(10L, "6800000000", "สวัสดีครับ"))
                .thenReturn(new ActionResponse(true, "ส่งข้อความสำเร็จ", dto));

        String json = """
                {
                    "senderStudentId": "6800000000",
                    "text": "สวัสดีครับ"
                }
                """;

        mockMvc.perform(post("/api/chat/events/10/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.data.id").value(55))
                .andExpect(jsonPath("$.data.text").value("สวัสดีครับ"));

        verify(chatWebSocketHandler).broadcastToRoom("10", dto);
    }

    @Test
    @DisplayName("POST /api/chat/events/{eventId}/messages คืนค่า 400 Bad Request เมื่อส่งไม่สำเร็จ")
    void sendEventMessage_ReturnsBadRequest() throws Exception {
        when(chatService.sendMessage(10L, "6800000009", "test"))
                .thenReturn(new ActionResponse(false, "คุณไม่มีสิทธิ์เข้าถึงห้องแชทของกิจกรรมนี้"));

        String json = """
                {
                    "senderStudentId": "6800000009",
                    "text": "test"
                }
                """;

        mockMvc.perform(post("/api/chat/events/10/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false));

        verifyNoInteractions(chatWebSocketHandler);
    }
}
