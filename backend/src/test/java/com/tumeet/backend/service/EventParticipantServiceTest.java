package com.tumeet.backend.service;

import com.tumeet.backend.dto.ActionResponse;
import com.tumeet.backend.dto.JoinEventRequest;
import com.tumeet.backend.dto.LeaveEventRequest;
import com.tumeet.backend.model.Event;
import com.tumeet.backend.model.EventParticipant;
import com.tumeet.backend.model.User;
import com.tumeet.backend.repository.EventParticipantRepository;
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
class EventParticipantServiceTest {

    @Mock
    private EventParticipantRepository eventParticipantRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EventParticipantService eventParticipantService;

    private User creatorUser;
    private User participantUser;
    private Event sampleEvent;

    @BeforeEach
    void setUp() {
        creatorUser = new User();
        creatorUser.setId(1L);
        creatorUser.setStudentId("6800000001");
        creatorUser.setEmail("creator@dome.tu.ac.th");

        participantUser = new User();
        participantUser.setId(2L);
        participantUser.setStudentId("6800000002");
        participantUser.setEmail("participant@dome.tu.ac.th");

        sampleEvent = new Event("ไปกินสุกี้", "food", 4, LocalDate.of(2026, 10, 25), "18:00", creatorUser);
        sampleEvent.setId(10L);
        sampleEvent.setCurrentParticipants(0);
        sampleEvent.setStatus("OPEN");
    }

    @Test
    @DisplayName("เข้าร่วมกิจกรรมสำเร็จ")
    void joinEvent_Success() {
        JoinEventRequest request = new JoinEventRequest(10L, "6800000002");

        when(userRepository.findByStudentId("6800000002")).thenReturn(Optional.of(participantUser));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(sampleEvent));
        when(eventParticipantRepository.existsByEventIdAndUserStudentId(10L, "6800000002")).thenReturn(false);

        ActionResponse response = eventParticipantService.joinEvent(request);

        assertTrue(response.isStatus());
        assertEquals("เข้าร่วมกิจกรรมสำเร็จ", response.getMessage());
        assertEquals(1, sampleEvent.getCurrentParticipants());
        verify(eventParticipantRepository, times(1)).save(any(EventParticipant.class));
        verify(eventRepository, times(1)).save(sampleEvent);
    }

    @Test
    @DisplayName("เข้าร่วมกิจกรรมล้มเหลวเมื่อเข้าร่วมซ้ำ")
    void joinEvent_AlreadyJoined() {
        JoinEventRequest request = new JoinEventRequest(10L, "6800000002");

        when(userRepository.findByStudentId("6800000002")).thenReturn(Optional.of(participantUser));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(sampleEvent));
        when(eventParticipantRepository.existsByEventIdAndUserStudentId(10L, "6800000002")).thenReturn(true);

        ActionResponse response = eventParticipantService.joinEvent(request);

        assertFalse(response.isStatus());
        assertTrue(response.getMessage().contains("คุณได้เข้าร่วมกิจกรรมนี้ไปแล้ว"));
        verify(eventParticipantRepository, never()).save(any(EventParticipant.class));
    }

    @Test
    @DisplayName("เข้าร่วมกิจกรรมล้มเหลวเมื่อกิจกรรมเต็มแล้ว")
    void joinEvent_EventFull() {
        sampleEvent.setCurrentParticipants(4);
        JoinEventRequest request = new JoinEventRequest(10L, "6800000002");

        when(userRepository.findByStudentId("6800000002")).thenReturn(Optional.of(participantUser));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(sampleEvent));

        ActionResponse response = eventParticipantService.joinEvent(request);

        assertFalse(response.isStatus());
        assertTrue(response.getMessage().contains("กิจกรรมนี้มีผู้เข้าร่วมเต็มแล้ว"));
        verify(eventParticipantRepository, never()).save(any(EventParticipant.class));
    }

    @Test
    @DisplayName("ยกเลิกการเข้าร่วมกิจกรรมสำเร็จ และลดจำนวนผู้เข้าร่วมลงเหลือ 0 ได้อย่างถูกต้อง")
    void leaveEvent_Success_ReducesToZero() {
        sampleEvent.setCurrentParticipants(1);
        LeaveEventRequest request = new LeaveEventRequest(10L, "6800000002");
        EventParticipant participant = new EventParticipant(sampleEvent, participantUser);

        when(userRepository.findByStudentId("6800000002")).thenReturn(Optional.of(participantUser));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(sampleEvent));
        when(eventParticipantRepository.findByEventIdAndUserStudentId(10L, "6800000002"))
                .thenReturn(Optional.of(participant));

        ActionResponse response = eventParticipantService.leaveEvent(request);

        assertTrue(response.isStatus());
        assertEquals("ยกเลิกการเข้าร่วมกิจกรรมสำเร็จ", response.getMessage());
        assertEquals(0, sampleEvent.getCurrentParticipants());
        verify(eventParticipantRepository, times(1)).delete(participant);
        verify(eventRepository, times(1)).save(sampleEvent);
    }

    @Test
    @DisplayName("ผู้สร้างกิจกรรมไม่สามารถออกจากกิจกรรมของตนเองได้")
    void leaveEvent_CreatorCannotLeave() {
        LeaveEventRequest request = new LeaveEventRequest(10L, "6800000001");

        when(userRepository.findByStudentId("6800000001")).thenReturn(Optional.of(creatorUser));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(sampleEvent));

        ActionResponse response = eventParticipantService.leaveEvent(request);

        assertFalse(response.isStatus());
        assertTrue(response.getMessage().contains("ผู้สร้างกิจกรรมไม่สามารถยกเลิกการเข้าร่วมได้"));
        verify(eventParticipantRepository, never()).delete(any());
    }

    @Test
    @DisplayName("ดึงรายชื่อผู้เข้าร่วมตาม Event ID สำเร็จ")
    void getParticipantsByEventId_Success() {
        EventParticipant p1 = new EventParticipant(sampleEvent, participantUser);
        when(eventParticipantRepository.findByEventId(10L)).thenReturn(List.of(p1));

        List<EventParticipant> result = eventParticipantService.getParticipantsByEventId(10L);

        assertEquals(1, result.size());
        assertEquals(participantUser, result.get(0).getUser());
    }
}
