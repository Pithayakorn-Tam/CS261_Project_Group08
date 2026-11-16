package com.tumeet.backend.controller;

import com.tumeet.backend.dto.ActionResponse;
import com.tumeet.backend.dto.NotificationDto;
import com.tumeet.backend.service.EventService;
import com.tumeet.backend.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EventService eventService;

    @BeforeEach
    void setUp() {
        NotificationController controller = new NotificationController(notificationService, eventService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /api/notifications คืนค่า 200 OK พร้อมรายการแจ้งเตือนและ unreadCount")
    void getNotifications_ReturnsOk() throws Exception {
        NotificationDto dto = new NotificationDto();
        dto.setId(1L);
        dto.setType(NotificationService.TYPE_JOIN_SUCCESS);
        dto.setMessage("คุณเข้าร่วมกิจกรรมสำเร็จ");
        dto.setIsRead(false);

        when(notificationService.getNotificationsForStudent("6800000000"))
                .thenReturn(new ActionResponse(true, "ดึงรายการแจ้งเตือนสำเร็จ",
                        Map.of("notifications", List.of(dto), "unreadCount", 1L)));

        mockMvc.perform(get("/api/notifications").param("studentId", "6800000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.data.unreadCount").value(1))
                .andExpect(jsonPath("$.data.notifications[0].message").value("คุณเข้าร่วมกิจกรรมสำเร็จ"));

        verify(eventService).syncAndCompleteExpiredEvents();
    }

    @Test
    @DisplayName("PUT /api/notifications/{id}/read คืนค่า 200 OK")
    void markAsRead_ReturnsOk() throws Exception {
        NotificationDto dto = new NotificationDto();
        dto.setId(5L);
        dto.setIsRead(true);

        when(notificationService.markAsRead(5L))
                .thenReturn(new ActionResponse(true, "อัปเดตสถานะอ่านแจ้งเตือนสำเร็จ", dto));

        mockMvc.perform(put("/api/notifications/5/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.data.isRead").value(true));
    }

    @Test
    @DisplayName("PUT /api/notifications/read-all คืนค่า 200 OK")
    void markAllAsRead_ReturnsOk() throws Exception {
        when(notificationService.markAllAsRead("6800000000"))
                .thenReturn(new ActionResponse(true, "อ่านแจ้งเตือนทั้งหมดแล้ว",
                        Map.of("updatedCount", 2, "unreadCount", 0L)));

        mockMvc.perform(put("/api/notifications/read-all").param("studentId", "6800000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.data.unreadCount").value(0));
    }
}
