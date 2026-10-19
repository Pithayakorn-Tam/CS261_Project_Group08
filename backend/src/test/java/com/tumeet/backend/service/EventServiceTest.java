package com.tumeet.backend.service;

import com.tumeet.backend.dto.CreateEventRequest;
import com.tumeet.backend.dto.EventDto;
import com.tumeet.backend.dto.EventResponse;
import com.tumeet.backend.model.Event;
import com.tumeet.backend.model.User;
import com.tumeet.backend.repository.EventRepository;
import com.tumeet.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EventService eventService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setStudentId("6800000001");
        mockUser.setEmail("mock01@dome.tu.ac.th");
        mockUser.setDisplayname_th("นักศึกษาจำลอง ทดสอบ01");
        mockUser.setFaculty("คณะวิทยาศาสตร์และเทคโนโลยี");
    }

    @Test
    @DisplayName("สร้าง Event สำเร็จเมื่อข้อมูลถูกต้อง")
    void createEvent_Success() {
        CreateEventRequest request = new CreateEventRequest();
        request.setEventType("food");
        request.setEventName("ไปกินตี๋น้อย");
        request.setEventSize(4);
        request.setEventDate("2026-10-25");
        request.setEventTime("18:00");
        request.setCreatorStudentId("6800000001");

        when(userRepository.findByStudentId("6800000001")).thenReturn(Optional.of(mockUser));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        EventResponse response = eventService.createEvent(request);

        assertTrue(response.isStatus());
        assertEquals("สร้างกิจกรรมสำเร็จ", response.getMessage());
        assertNotNull(response.getData());

        EventDto dto = (EventDto) response.getData();
        assertEquals(100L, dto.getId());
        assertEquals("ไปกินตี๋น้อย", dto.getEventName());
        assertEquals("food", dto.getEventType());
        assertEquals(4, dto.getEventSize());
        assertEquals(1, dto.getCurrentParticipants());
        assertEquals(LocalDate.of(2026, 10, 25), dto.getEventDate());
        assertEquals("18:00", dto.getEventTime());
        assertEquals("OPEN", dto.getStatus());
        assertNotNull(dto.getCreator());
        assertEquals("6800000001", dto.getCreator().getStudentId());
        assertEquals("นักศึกษาจำลอง ทดสอบ01", dto.getCreator().getDisplayname_th());

        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("สร้าง Event ล้มเหลวเมื่อไม่พบผู้ใช้ในระบบ")
    void createEvent_UserNotFound() {
        CreateEventRequest request = new CreateEventRequest();
        request.setCreatorStudentId("9999999999");
        request.setEventName("อ่านหนังสือ");
        request.setEventType("study");
        request.setEventSize(2);
        request.setEventDate("2026-10-25");
        request.setEventTime("18:00");

        when(userRepository.findByStudentId("9999999999")).thenReturn(Optional.empty());

        EventResponse response = eventService.createEvent(request);

        assertFalse(response.isStatus());
        assertTrue(response.getMessage().contains("ไม่พบข้อมูลผู้ใช้"));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("สร้าง Event ล้มเหลวเมื่อ eventSize น้อยกว่า 1")
    void createEvent_InvalidEventSize() {
        CreateEventRequest request = new CreateEventRequest();
        request.setCreatorStudentId("6800000001");
        request.setEventName("ไปกินตี๋น้อย");
        request.setEventType("food");
        request.setEventSize(0);
        request.setEventDate("2026-10-25");
        request.setEventTime("18:00");

        when(userRepository.findByStudentId("6800000001")).thenReturn(Optional.of(mockUser));

        EventResponse response = eventService.createEvent(request);

        assertFalse(response.isStatus());
        assertTrue(response.getMessage().contains("มากกว่าหรือเท่ากับ 1"));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("ดึงรายการ Event ที่เปิดรับอยู่สำเร็จ")
    void getAllOpenEvents_Success() {
        Event e1 = new Event("บอร์ดเกม", "bordgame", 6, LocalDate.of(2026, 10, 20), "13:00", mockUser);
        e1.setId(1L);
        Event e2 = new Event("ไปกินตี๋น้อย", "food", 4, LocalDate.of(2026, 10, 25), "18:00", mockUser);
        e2.setId(2L);

        when(eventRepository.findByStatusOrderByEventDateAsc("OPEN")).thenReturn(List.of(e1, e2));

        EventResponse response = eventService.getAllOpenEvents();

        assertTrue(response.isStatus());
        assertNotNull(response.getData());
        @SuppressWarnings("unchecked")
        List<EventDto> dtos = (List<EventDto>) response.getData();
        assertEquals(2, dtos.size());
        assertEquals("บอร์ดเกม", dtos.get(0).getEventName());
        assertEquals("ไปกินตี๋น้อย", dtos.get(1).getEventName());
    }
}
