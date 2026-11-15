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
    private LocalDate validFutureDate;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setStudentId("6800000001");
        mockUser.setEmail("mock01@dome.tu.ac.th");
        mockUser.setDisplayname_th("นักศึกษาจำลอง ทดสอบ01");
        mockUser.setFaculty("คณะวิทยาศาสตร์และเทคโนโลยี");

        validFutureDate = LocalDate.now().plusDays(2);
    }

    @Test
    @DisplayName("สร้าง Event สำเร็จเมื่อข้อมูลถูกต้อง (อยู่ในช่วงไม่เกิน 7 วัน)")
    void createEvent_Success() {
        CreateEventRequest request = new CreateEventRequest();
        request.setEventType("food");
        request.setEventName("ไปกินตี๋น้อย");
        request.setEventSize(4);
        request.setEventDate(validFutureDate.toString());
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
        assertEquals(validFutureDate, dto.getEventDate());
        assertEquals("18:00", dto.getEventTime());
        assertEquals("OPEN", dto.getStatus());
        assertEquals("UPCOMING", dto.getLifecycleStatus());
        assertNotNull(dto.getCreator());
        assertEquals("6800000001", dto.getCreator().getStudentId());
        assertEquals("นักศึกษาจำลอง ทดสอบ01", dto.getCreator().getDisplayname_th());
        assertEquals("No Badge", dto.getCreator().getBadge());

        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("สร้าง Event ล้มเหลวเมื่อวันที่อยู่ในอดีต")
    void createEvent_PastDate_Fails() {
        CreateEventRequest request = new CreateEventRequest();
        request.setCreatorStudentId("6800000001");
        request.setEventName("ไปกินตี๋น้อย");
        request.setEventType("food");
        request.setEventSize(4);
        request.setEventDate(LocalDate.now().minusDays(1).toString());
        request.setEventTime("18:00");

        when(userRepository.findByStudentId("6800000001")).thenReturn(Optional.of(mockUser));

        EventResponse response = eventService.createEvent(request);

        assertFalse(response.isStatus());
        assertTrue(response.getMessage().contains("ไม่สามารถสร้างกิจกรรมในอดีตได้"));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("สร้าง Event ล้มเหลวเมื่อตั้งวันที่ล่วงหน้าเกิน 7 วัน")
    void createEvent_MoreThan7DaysAdvance_Fails() {
        CreateEventRequest request = new CreateEventRequest();
        request.setCreatorStudentId("6800000001");
        request.setEventName("ไปกินตี๋น้อย");
        request.setEventType("food");
        request.setEventSize(4);
        request.setEventDate(LocalDate.now().plusDays(8).toString());
        request.setEventTime("18:00");

        when(userRepository.findByStudentId("6800000001")).thenReturn(Optional.of(mockUser));

        EventResponse response = eventService.createEvent(request);

        assertFalse(response.isStatus());
        assertTrue(response.getMessage().contains("สามารถสร้างกิจกรรมล่วงหน้าได้ไม่เกิน 7 วัน"));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("สร้าง Event ล้มเหลวเมื่อไม่พบผู้ใช้ในระบบ")
    void createEvent_UserNotFound() {
        CreateEventRequest request = new CreateEventRequest();
        request.setCreatorStudentId("9999999999");
        request.setEventName("อ่านหนังสือ");
        request.setEventType("study");
        request.setEventSize(2);
        request.setEventDate(validFutureDate.toString());
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
        request.setEventDate(validFutureDate.toString());
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
        Event e1 = new Event("บอร์ดเกม", "bordgame", 6, validFutureDate, "13:00", mockUser);
        e1.setId(1L);
        Event e2 = new Event("ไปกินตี๋น้อย", "food", 4, validFutureDate, "18:00", mockUser);
        e2.setId(2L);

        when(eventRepository.findByStatusOrderByIdAsc("OPEN")).thenReturn(List.of(e1, e2));

        EventResponse response = eventService.getAllOpenEvents();

        assertTrue(response.isStatus());
        assertNotNull(response.getData());
        @SuppressWarnings("unchecked")
        List<EventDto> dtos = (List<EventDto>) response.getData();
        assertEquals(2, dtos.size());
        assertEquals("บอร์ดเกม", dtos.get(0).getEventName());
        assertEquals("ไปกินตี๋น้อย", dtos.get(1).getEventName());
    }

    @Test
    @DisplayName("เข้าร่วมและยกเลิกการเข้าร่วมกิจกรรมผ่าน EventService สำเร็จ")
    void joinAndLeaveEvent_Success() {
        Event event = new Event("ตีแบดมินตัน", "sport", "ยิม มธ.", 4, validFutureDate, "20.00", mockUser);
        event.setId(3L);
        event.setCurrentParticipants(2);

        User participantUser = new User();
        participantUser.setId(2L);
        participantUser.setStudentId("6800000000");
        participantUser.setEmail("mock00@dome.tu.ac.th");
        participantUser.setDisplayname_th("นักศึกษาจำลอง ทดสอบ00");

        when(eventRepository.findById(3L)).thenReturn(Optional.of(event));
        when(userRepository.findByStudentId("6800000000")).thenReturn(Optional.of(participantUser));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 1. ทดสอบกดเข้าร่วม (Join)
        EventResponse joinRes = eventService.joinEvent(3L, "6800000000");
        assertTrue(joinRes.isStatus());
        assertEquals("เข้าร่วมกิจกรรมสำเร็จ", joinRes.getMessage());
        assertEquals(3, event.getCurrentParticipants());
        assertTrue(event.getParticipants().contains(participantUser));

        // 2. ทดสอบกดยกเลิกการเข้าร่วม (Leave)
        EventResponse leaveRes = eventService.leaveEvent(3L, "6800000000");
        assertTrue(leaveRes.isStatus());
        assertEquals("ยกเลิกการเข้าร่วมกิจกรรมสำเร็จ", leaveRes.getMessage());
        assertEquals(2, event.getCurrentParticipants());
        assertFalse(event.getParticipants().contains(participantUser));

        // 3. ทดสอบผู้สร้างไม่สามารถกดเข้าร่วมกิจกรรมของตนเองได้
        when(userRepository.findByStudentId("6800000001")).thenReturn(Optional.of(mockUser));
        EventResponse creatorJoinRes = eventService.joinEvent(3L, "6800000001");
        assertFalse(creatorJoinRes.isStatus());
        assertTrue(creatorJoinRes.getMessage().contains("ผู้สร้างกิจกรรมไม่สามารถเข้าร่วมกิจกรรมของตนเองได้"));
    }

    @Test
    @DisplayName("US-9: คำนวณยศ (Badge) ตามผลรวมกิจกรรมที่สร้างและเข้าร่วมสำเร็จ")
    void userBadgeCalculation_Success() {
        User user = new User();
        assertEquals(0, user.getCompletedCreatedCount());
        assertEquals(0, user.getCompletedJoinedCount());
        assertEquals("No Badge", user.getBadge());

        user.setCompletedCreatedCount(1);
        user.setCompletedJoinedCount(0);
        assertEquals("Newly Bee", user.getBadge());

        user.setCompletedCreatedCount(2);
        user.setCompletedJoinedCount(2);
        assertEquals("Newly Bee", user.getBadge());

        user.setCompletedCreatedCount(2);
        user.setCompletedJoinedCount(3);
        assertEquals("Baby Bloom", user.getBadge());

        user.setCompletedCreatedCount(5);
        user.setCompletedJoinedCount(5);
        assertEquals("Happy Blossom", user.getBadge());

        user.setCompletedCreatedCount(12);
        user.setCompletedJoinedCount(8);
        assertEquals("Super Bouquet", user.getBadge());
    }

    @Test
    @DisplayName("US-9: กิจกรรมที่หมดเวลาแล้วและมีสมาชิกตั้งแต่ 2 คนขึ้นไป จะเปลี่ยนเป็น COMPLETED และแจกแต้ม +1")
    void syncAndCompleteExpiredEvents_AwardsPointsWhenAtLeastTwoMembers() {
        User participant = new User();
        participant.setId(2L);
        participant.setStudentId("6800000002");

        Event pastEvent = new Event("อ่านหนังสือ", "study", "ห้องสมุด", 4, LocalDate.now().minusDays(1), "18:00", mockUser);
        pastEvent.setId(10L);
        pastEvent.addParticipant(participant); // currentParticipants becomes 2

        when(eventRepository.findByLifecycleStatus("UPCOMING")).thenReturn(List.of(pastEvent));

        eventService.syncAndCompleteExpiredEvents();

        assertEquals("COMPLETED", pastEvent.getLifecycleStatus());
        assertEquals("CLOSED", pastEvent.getStatus());
        assertEquals(1, mockUser.getCompletedCreatedCount());
        assertEquals(0, mockUser.getCompletedJoinedCount());
        assertEquals("Newly Bee", mockUser.getBadge());

        assertEquals(0, participant.getCompletedCreatedCount());
        assertEquals(1, participant.getCompletedJoinedCount());
        assertEquals("Newly Bee", participant.getBadge());

        verify(userRepository, times(1)).save(mockUser);
        verify(userRepository, times(1)).save(participant);
        verify(eventRepository, times(1)).save(pastEvent);
    }

    @Test
    @DisplayName("US-9: กิจกรรมที่หมดเวลาแต่มีแค่ผู้สร้างคนเดียว (currentParticipants < 2) จะเปลี่ยนเป็น COMPLETED แต่ไม่แจกแต้ม +1")
    void syncAndCompleteExpiredEvents_NoPointsWhenOnlyCreator() {
        Event soloExpiredEvent = new Event("วิ่งคนเดียว", "sport", "สนาม", 4, LocalDate.now().minusDays(1), "18:00", mockUser);
        soloExpiredEvent.setId(11L);
        soloExpiredEvent.setCurrentParticipants(1);

        when(eventRepository.findByLifecycleStatus("UPCOMING")).thenReturn(List.of(soloExpiredEvent));

        eventService.syncAndCompleteExpiredEvents();

        assertEquals("COMPLETED", soloExpiredEvent.getLifecycleStatus());
        assertEquals("CLOSED", soloExpiredEvent.getStatus());
        assertEquals(0, mockUser.getCompletedCreatedCount());
        assertEquals("No Badge", mockUser.getBadge());

        verify(userRepository, never()).save(any(User.class));
        verify(eventRepository, times(1)).save(soloExpiredEvent);
    }

    @Test
    @DisplayName("US-9: ผู้สร้างกดยกเลิกกิจกรรม จะเปลี่ยน lifecycleStatus เป็น CANCELLED และ status เป็น CLOSED โดยไม่ลบออกจาก DB")
    void leaveEvent_ByCreator_CancelsWithoutDeleting() {
        Event event = new Event("ตีแบดมินตัน", "sport", "ยิม มธ.", 4, validFutureDate, "20:00", mockUser);
        event.setId(12L);

        when(eventRepository.findById(12L)).thenReturn(Optional.of(event));
        when(userRepository.findByStudentId("6800000001")).thenReturn(Optional.of(mockUser));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventResponse response = eventService.leaveEvent(12L, "6800000001");

        assertTrue(response.isStatus());
        assertEquals("ยกเลิกกิจกรรมสำเร็จ", response.getMessage());
        assertEquals("CANCELLED", event.getLifecycleStatus());
        assertEquals("CLOSED", event.getStatus());

        verify(eventRepository, never()).delete(any(Event.class));
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    @DisplayName("US-9: ไม่สามารถกด Join หรือ Leave ในกิจกรรมที่ COMPLETED หรือ CANCELLED ไปแล้วได้")
    void joinAndLeaveEvent_BlockedWhenCompletedOrCancelled() {
        Event completedEvent = new Event("อ่านหนังสือ", "study", "ห้องสมุด", 4, validFutureDate, "18:00", mockUser);
        completedEvent.setId(13L);
        completedEvent.setLifecycleStatus("COMPLETED");

        User participant = new User();
        participant.setId(2L);
        participant.setStudentId("6800000002");

        when(eventRepository.findById(13L)).thenReturn(Optional.of(completedEvent));
        when(userRepository.findByStudentId("6800000002")).thenReturn(Optional.of(participant));

        EventResponse joinRes = eventService.joinEvent(13L, "6800000002");
        assertFalse(joinRes.isStatus());
        assertEquals("ไม่สามารถทำรายการได้เนื่องจากกิจกรรมนี้สิ้นสุดหรือถูกยกเลิกแล้ว", joinRes.getMessage());

        completedEvent.setLifecycleStatus("CANCELLED");
        EventResponse leaveRes = eventService.leaveEvent(13L, "6800000002");
        assertFalse(leaveRes.isStatus());
        assertEquals("ไม่สามารถทำรายการได้เนื่องจากกิจกรรมนี้สิ้นสุดหรือถูกยกเลิกแล้ว", leaveRes.getMessage());
    }
}
