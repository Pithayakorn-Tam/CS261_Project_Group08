package com.tumeet.backend.controller;

import com.tumeet.backend.dto.ActionResponse;
import com.tumeet.backend.dto.JoinEventRequest;
import com.tumeet.backend.dto.LeaveEventRequest;
import com.tumeet.backend.service.EventParticipantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EventParticipantControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EventParticipantService eventParticipantService;

    @InjectMocks
    private EventParticipantController eventParticipantController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventParticipantController).build();
    }

    @Test
    @DisplayName("POST /api/events/join คืนค่า 200 OK เมื่อเข้าร่วมสำเร็จ")
    void joinEvent_ReturnsOk() throws Exception {
        String requestJson = """
                {
                    "eventId": 1,
                    "studentId": "6800000002"
                }
                """;

        when(eventParticipantService.joinEvent(any(JoinEventRequest.class)))
                .thenReturn(new ActionResponse(true, "เข้าร่วมกิจกรรมสำเร็จ"));

        mockMvc.perform(post("/api/events/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("เข้าร่วมกิจกรรมสำเร็จ"));
    }

    @Test
    @DisplayName("POST /api/events/join คืนค่า 400 Bad Request เมื่อเข้าร่วมไม่สำเร็จ")
    void joinEvent_ReturnsBadRequest() throws Exception {
        String requestJson = """
                {
                    "eventId": 1,
                    "studentId": "6800000002"
                }
                """;

        when(eventParticipantService.joinEvent(any(JoinEventRequest.class)))
                .thenReturn(new ActionResponse(false, "คุณได้เข้าร่วมกิจกรรมนี้ไปแล้ว"));

        mockMvc.perform(post("/api/events/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("คุณได้เข้าร่วมกิจกรรมนี้ไปแล้ว"));
    }

    @Test
    @DisplayName("POST /api/events/leave คืนค่า 200 OK เมื่อยกเลิกเข้าร่วมสำเร็จ")
    void leaveEvent_ReturnsOk() throws Exception {
        String requestJson = """
                {
                    "eventId": 1,
                    "studentId": "6800000002"
                }
                """;

        when(eventParticipantService.leaveEvent(any(LeaveEventRequest.class)))
                .thenReturn(new ActionResponse(true, "ยกเลิกการเข้าร่วมกิจกรรมสำเร็จ"));

        mockMvc.perform(post("/api/events/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("ยกเลิกการเข้าร่วมกิจกรรมสำเร็จ"));
    }

    @Test
    @DisplayName("GET /api/events/{id}/participants คืนค่า 200 OK")
    void getParticipants_ReturnsOk() throws Exception {
        when(eventParticipantService.getParticipantsByEventId(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/events/1/participants"))
                .andExpect(status().isOk());
    }
}
