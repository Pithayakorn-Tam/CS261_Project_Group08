package com.tumeet.backend.service;

import com.tumeet.backend.dto.ActionResponse;
import com.tumeet.backend.dto.ChatMessageDto;
import com.tumeet.backend.dto.ChatRoomDto;
import com.tumeet.backend.model.ChatMessage;
import com.tumeet.backend.model.Event;
import com.tumeet.backend.model.User;
import com.tumeet.backend.repository.ChatMessageRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatService chatService;

    private User creator;
    private User participant;
    private User outsider;
    private Event activeEvent;
    private Event joinedEvent;
    private Event cancelledEvent;

    @BeforeEach
    void setUp() {
        creator = new User();
        creator.setId(1L);
        creator.setStudentId("6800000000");
        creator.setEmail("mock00@dome.tu.ac.th");
        creator.setUsername("หมูกรอบ");
        creator.setDisplayname_th("นักศึกษาจำลอง ทดสอบ00");

        participant = new User();
        participant.setId(2L);
        participant.setStudentId("6800000002");
        participant.setEmail("mock02@dome.tu.ac.th");
        participant.setDisplayname_th("นักศึกษาจำลอง ทดสอบ02");

        outsider = new User();
        outsider.setId(9L);
        outsider.setStudentId("6800000009");
        outsider.setEmail("mock09@dome.tu.ac.th");
        outsider.setDisplayname_th("นักศึกษาจำลอง ทดสอบ09");

        activeEvent = new Event();
        activeEvent.setId(10L);
        activeEvent.setEventName("หมูกระทะ");
        activeEvent.setEventType("food");
        activeEvent.setEventSize(6);
        activeEvent.setCurrentParticipants(2);
        activeEvent.setEventDate(LocalDate.now().plusDays(1));
        activeEvent.setEventTime("19.00");
        activeEvent.setStatus("OPEN");
        activeEvent.setLifecycleStatus("UPCOMING");
        activeEvent.setCreator(creator);
        activeEvent.setParticipants(Set.of(participant));

        joinedEvent = new Event();
        joinedEvent.setId(11L);
        joinedEvent.setEventName("อ่านหนังสือ");
        joinedEvent.setEventType("study");
        joinedEvent.setEventSize(4);
        joinedEvent.setCurrentParticipants(2);
        joinedEvent.setEventDate(LocalDate.now().plusDays(2));
        joinedEvent.setEventTime("18.00");
        joinedEvent.setStatus("OPEN");
        joinedEvent.setLifecycleStatus("UPCOMING");
        joinedEvent.setCreator(participant);
        joinedEvent.setParticipants(Set.of(creator));

        cancelledEvent = new Event();
        cancelledEvent.setId(12L);
        cancelledEvent.setEventName("กิจกรรมยกเลิก");
        cancelledEvent.setEventType("other");
        cancelledEvent.setEventSize(4);
        cancelledEvent.setCurrentParticipants(1);
        cancelledEvent.setEventDate(LocalDate.now().plusDays(3));
        cancelledEvent.setEventTime("17.00");
        cancelledEvent.setStatus("CLOSED");
        cancelledEvent.setLifecycleStatus("CANCELLED");
        cancelledEvent.setCreator(creator);
    }

    @Test
    @DisplayName("ดึงรายการห้องแชทสำเร็จ รวมกิจกรรมที่สร้างและเข้าร่วม ตัดกิจกรรม CANCELLED และแนบข้อความล่าสุด")
    @SuppressWarnings("unchecked")
    void getChatRoomsForUser_Success() {
        ChatMessage latest = new ChatMessage(activeEvent, participant, "เจอกัน 1 ทุ่มนะ");
        latest.setId(100L);
        latest.setSentAt(LocalDateTime.of(2026, 11, 14, 13, 0));

        when(userRepository.findByStudentId("6800000000")).thenReturn(Optional.of(creator));
        when(eventRepository.findByCreator(creator)).thenReturn(List.of(activeEvent, cancelledEvent));
        when(eventRepository.findByParticipantsContaining(creator)).thenReturn(List.of(activeEvent, joinedEvent));
        when(chatMessageRepository.findTopByEventIdOrderBySentAtDesc(10L)).thenReturn(Optional.of(latest));
        when(chatMessageRepository.findTopByEventIdOrderBySentAtDesc(11L)).thenReturn(Optional.empty());

        ActionResponse response = chatService.getChatRoomsForUser("6800000000");

        assertTrue(response.isStatus());
        List<ChatRoomDto> rooms = (List<ChatRoomDto>) response.getData();
        assertEquals(2, rooms.size());
        assertEquals(10L, rooms.get(0).getEventId());
        assertEquals("หมูกระทะ", rooms.get(0).getEventName());
        assertEquals("เจอกัน 1 ทุ่มนะ", rooms.get(0).getLastMessage());
        assertEquals(latest.getSentAt(), rooms.get(0).getLastMessageTime());

        assertEquals(11L, rooms.get(1).getEventId());
        assertNull(rooms.get(1).getLastMessage());
    }

    @Test
    @DisplayName("ดึงรายการห้องแชทไม่สำเร็จเมื่อไม่พบผู้ใช้")
    void getChatRoomsForUser_UserNotFound() {
        when(userRepository.findByStudentId("9999999999")).thenReturn(Optional.empty());

        ActionResponse response = chatService.getChatRoomsForUser("9999999999");

        assertFalse(response.isStatus());
    }

    @Test
    @DisplayName("ดึงประวัติข้อความแชทสำเร็จสำหรับสมาชิกในกิจกรรม")
    @SuppressWarnings("unchecked")
    void getMessagesByEvent_MemberSuccess() {
        ChatMessage msg1 = new ChatMessage(activeEvent, creator, "สวัสดีทุกคน");
        msg1.setId(1L);
        msg1.setSentAt(LocalDateTime.of(2026, 11, 14, 12, 0));

        ChatMessage msg2 = new ChatMessage(activeEvent, participant, "พร้อมแล้วครับ");
        msg2.setId(2L);
        msg2.setSentAt(LocalDateTime.of(2026, 11, 14, 12, 5));

        when(eventRepository.findById(10L)).thenReturn(Optional.of(activeEvent));
        when(userRepository.findByStudentId("6800000002")).thenReturn(Optional.of(participant));
        when(chatMessageRepository.findByEventIdOrderBySentAtAsc(10L)).thenReturn(List.of(msg1, msg2));

        ActionResponse response = chatService.getMessagesByEvent(10L, "6800000002");

        assertTrue(response.isStatus());
        List<ChatMessageDto> dtos = (List<ChatMessageDto>) response.getData();
        assertEquals(2, dtos.size());
        assertEquals("6800000000", dtos.get(0).getSenderStudentId());
        assertEquals("หมูกรอบ", dtos.get(0).getSenderName());
        assertEquals("สวัสดีทุกคน", dtos.get(0).getText());
        assertEquals("6800000002", dtos.get(1).getSenderStudentId());
        assertEquals("นักศึกษาจำลอง ทดสอบ02", dtos.get(1).getSenderName());
    }

    @Test
    @DisplayName("ดึงประวัติข้อความแชทล้มเหลวหากไม่ใช่สมาชิกของกิจกรรม")
    void getMessagesByEvent_OutsiderForbidden() {
        when(eventRepository.findById(10L)).thenReturn(Optional.of(activeEvent));
        when(userRepository.findByStudentId("6800000009")).thenReturn(Optional.of(outsider));

        ActionResponse response = chatService.getMessagesByEvent(10L, "6800000009");

        assertFalse(response.isStatus());
        assertEquals("คุณไม่มีสิทธิ์เข้าถึงห้องแชทของกิจกรรมนี้", response.getMessage());
        verify(chatMessageRepository, never()).findByEventIdOrderBySentAtAsc(any());
    }

    @Test
    @DisplayName("ส่งข้อความสำเร็จและบันทึกลง DB สำหรับผู้เข้าร่วมกิจกรรม")
    void sendMessage_Success() {
        when(eventRepository.findById(10L)).thenReturn(Optional.of(activeEvent));
        when(userRepository.findByStudentId("6800000002")).thenReturn(Optional.of(participant));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(inv -> {
            ChatMessage m = inv.getArgument(0);
            m.setId(55L);
            m.setSentAt(LocalDateTime.of(2026, 11, 14, 13, 15));
            return m;
        });

        ActionResponse response = chatService.sendMessage(10L, "6800000002", "  ถึงร้านแล้วนะครับ  ");

        assertTrue(response.isStatus());
        assertInstanceOf(ChatMessageDto.class, response.getData());
        ChatMessageDto dto = (ChatMessageDto) response.getData();
        assertEquals(55L, dto.getId());
        assertEquals(10L, dto.getEventId());
        assertEquals("10", dto.getRoomId());
        assertEquals("6800000002", dto.getSenderStudentId());
        assertEquals("ถึงร้านแล้วนะครับ", dto.getText());
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("ปฏิเสธการส่งข้อความว่าง ข้อความเกิน 2000 ตัวอักษร และกิจกรรมที่ยกเลิกแล้ว")
    void sendMessage_ValidationFailures() {
        ActionResponse blankRes = chatService.sendMessage(10L, "6800000000", "   ");
        assertFalse(blankRes.isStatus());

        String tooLong = "a".repeat(2001);
        ActionResponse tooLongRes = chatService.sendMessage(10L, "6800000000", tooLong);
        assertFalse(tooLongRes.isStatus());

        when(eventRepository.findById(12L)).thenReturn(Optional.of(cancelledEvent));
        when(userRepository.findByStudentId("6800000000")).thenReturn(Optional.of(creator));
        ActionResponse cancelledRes = chatService.sendMessage(12L, "6800000000", "ฮัลโหล");
        assertFalse(cancelledRes.isStatus());
        assertEquals("ไม่สามารถใช้งานแชทได้เนื่องจากกิจกรรมนี้ถูกยกเลิกแล้ว", cancelledRes.getMessage());

        verify(chatMessageRepository, never()).save(any());
    }
}
