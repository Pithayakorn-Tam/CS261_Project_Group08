package com.tumeet.backend.controller;

import com.tumeet.backend.dto.CreateEventRequest;
import com.tumeet.backend.dto.CreatorDto;
import com.tumeet.backend.dto.EventDto;
import com.tumeet.backend.dto.EventResponse;
import com.tumeet.backend.service.EventService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
    }

    @Test
    @DisplayName("POST /api/events ส่งกลับ 201 Created เมื่อสร้างกิจกรรมสำเร็จ")
    void createEvent_ReturnsCreated() throws Exception {
        String requestJson = """
                {
                    "eventType": "food",
                    "eventName": "ไปกินตี๋น้อย",
                    "eventSize": 4,
                    "eventDate": "2026-10-25",
                    "eventTime": "18:00",
                    "creatorStudentId": "6800000001"
                }
                """;

        EventDto dto = new EventDto();
        dto.setId(1L);
        dto.setEventName("ไปกินตี๋น้อย");
        dto.setEventType("food");
        dto.setEventSize(4);
        dto.setCurrentParticipants(1);
        dto.setEventDate(LocalDate.of(2026, 10, 25));
        dto.setEventTime("18:00");
        dto.setStatus("OPEN");
        dto.setCreator(new CreatorDto("6800000001", "ทดสอบ", "วท.", "test@dome.tu.ac.th"));

        when(eventService.createEvent(any(CreateEventRequest.class)))
                .thenReturn(EventResponse.success("สร้างกิจกรรมสำเร็จ", dto));

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("สร้างกิจกรรมสำเร็จ"))
                .andExpect(jsonPath("$.data.eventName").value("ไปกินตี๋น้อย"))
                .andExpect(jsonPath("$.data.creator.studentId").value("6800000001"));
    }

    @Test
    @DisplayName("POST /api/events ส่งกลับ 400 Bad Request เมื่อข้อมูลไม่ถูกต้อง")
    void createEvent_ReturnsBadRequest() throws Exception {
        String requestJson = """
                {
                    "creatorStudentId": "9999999999"
                }
                """;

        when(eventService.createEvent(any(CreateEventRequest.class)))
                .thenReturn(EventResponse.error("ไม่พบข้อมูลผู้ใช้สำหรับรหัสนักศึกษา: 9999999999"));

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("ไม่พบข้อมูลผู้ใช้สำหรับรหัสนักศึกษา: 9999999999"));
    }

    @Test
    @DisplayName("GET /api/events ส่งกลับ 200 OK พร้อมรายการกิจกรรม")
    void getEvents_ReturnsOk() throws Exception {
        EventDto dto = new EventDto();
        dto.setId(1L);
        dto.setEventName("ไปกินตี๋น้อย");
        dto.setStatus("OPEN");

        when(eventService.getAllOpenEvents())
                .thenReturn(EventResponse.success("ดึงรายการกิจกรรมที่เปิดรับอยู่สำเร็จ", List.of(dto)));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.data[0].eventName").value("ไปกินตี๋น้อย"));
    }
}
