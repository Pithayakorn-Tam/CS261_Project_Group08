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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ChatService {

    public static final int MAX_MESSAGE_LENGTH = 2000;

    private final ChatMessageRepository chatMessageRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public ChatService(ChatMessageRepository chatMessageRepository,
                       EventRepository eventRepository,
                       UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    /**
     * คืนค่ารายการห้องแชท (กิจกรรมที่ไม่ได้ถูกยกเลิก) ที่ผู้ใช้เป็นผู้สร้างหรือผู้เข้าร่วม
     */
    @Transactional(readOnly = true)
    public ActionResponse getChatRoomsForUser(String studentId) {
        if (studentId == null || studentId.isBlank()) {
            return new ActionResponse(false, "กรุณาระบุรหัสนักศึกษา");
        }

        Optional<User> userOpt = userRepository.findByStudentId(studentId.trim());
        if (userOpt.isEmpty()) {
            return new ActionResponse(false, "ไม่พบข้อมูลผู้ใช้สำหรับรหัสนักศึกษา: " + studentId);
        }
        User user = userOpt.get();

        Map<Long, Event> uniqueEvents = new LinkedHashMap<>();
        List<Event> createdEvents = eventRepository.findByCreator(user);
        if (createdEvents != null) {
            for (Event ev : createdEvents) {
                if (ev != null && ev.getId() != null && !isEventCancelled(ev)) {
                    uniqueEvents.put(ev.getId(), ev);
                }
            }
        }

        List<Event> joinedEvents = eventRepository.findByParticipantsContaining(user);
        if (joinedEvents != null) {
            for (Event ev : joinedEvents) {
                if (ev != null && ev.getId() != null && !isEventCancelled(ev)) {
                    uniqueEvents.putIfAbsent(ev.getId(), ev);
                }
            }
        }

        List<ChatRoomDto> rooms = new ArrayList<>();
        for (Event event : uniqueEvents.values()) {
            ChatMessage latestMessage = chatMessageRepository
                    .findTopByEventIdOrderBySentAtDesc(event.getId())
                    .orElse(null);
            rooms.add(ChatRoomDto.fromEvent(event, latestMessage));
        }

        return new ActionResponse(true, "ดึงรายการห้องแชทสำเร็จ", rooms);
    }

    /**
     * ดึงประวัติข้อความแชททั้งหมดของกิจกรรมเรียงตามเวลาจากเก่าไปใหม่
     */
    @Transactional(readOnly = true)
    public ActionResponse getMessagesByEvent(Long eventId, String studentId) {
        MemberValidationResult validation = validateEventMembership(eventId, studentId);
        if (validation.error() != null) {
            return validation.error();
        }

        List<ChatMessage> messages = chatMessageRepository.findByEventIdOrderBySentAtAsc(eventId);
        List<ChatMessageDto> dtos = messages.stream()
                .map(ChatMessageDto::fromEntity)
                .toList();

        return new ActionResponse(true, "ดึงประวัติข้อความแชทสำเร็จ", dtos);
    }

    /**
     * บันทึกข้อความแชทใหม่ลงฐานข้อมูลหลังตรวจสอบสิทธิ์สมาชิกและความยาวข้อความแล้ว
     */
    @Transactional
    public ActionResponse sendMessage(Long eventId, String senderStudentId, String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ActionResponse(false, "กรุณากรอกข้อความก่อนส่ง");
        }

        String trimmedText = text.trim();
        if (trimmedText.length() > MAX_MESSAGE_LENGTH) {
            return new ActionResponse(false, "ข้อความยาวเกินกำหนด (สูงสุด 2000 ตัวอักษร)");
        }

        MemberValidationResult validation = validateEventMembership(eventId, senderStudentId);
        if (validation.error() != null) {
            return validation.error();
        }

        ChatMessage message = new ChatMessage(validation.event(), validation.user(), trimmedText);
        ChatMessage saved = chatMessageRepository.save(message);
        if (saved == null) {
            saved = message;
        }
        return new ActionResponse(true, "ส่งข้อความสำเร็จ", ChatMessageDto.fromEntity(saved));
    }

    private MemberValidationResult validateEventMembership(Long eventId, String studentId) {
        if (eventId == null || studentId == null || studentId.isBlank()) {
            return new MemberValidationResult(null, null, new ActionResponse(false, "กรุณาระบุรหัสกิจกรรมและรหัสนักศึกษา"));
        }

        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return new MemberValidationResult(null, null, new ActionResponse(false, "ไม่พบกิจกรรมรหัส: " + eventId));
        }

        Optional<User> userOpt = userRepository.findByStudentId(studentId.trim());
        if (userOpt.isEmpty()) {
            return new MemberValidationResult(null, null, new ActionResponse(false, "ไม่พบข้อมูลผู้ใช้สำหรับรหัสนักศึกษา: " + studentId));
        }

        Event event = eventOpt.get();
        User user = userOpt.get();

        if (isEventCancelled(event)) {
            return new MemberValidationResult(null, null, new ActionResponse(false, "ไม่สามารถใช้งานแชทได้เนื่องจากกิจกรรมนี้ถูกยกเลิกแล้ว"));
        }

        if (!isEventMember(event, user)) {
            return new MemberValidationResult(null, null, new ActionResponse(false, "คุณไม่มีสิทธิ์เข้าถึงห้องแชทของกิจกรรมนี้"));
        }

        return new MemberValidationResult(event, user, null);
    }

    private boolean isEventCancelled(Event event) {
        return "CANCELLED".equalsIgnoreCase(event.getLifecycleStatus())
                || "CANCELLED".equalsIgnoreCase(event.getStatus());
    }

    private boolean isEventMember(Event event, User user) {
        if (event.getCreator() != null && user.getStudentId().equals(event.getCreator().getStudentId())) {
            return true;
        }
        if (event.getParticipants() != null) {
            for (User participant : event.getParticipants()) {
                if (participant != null && user.getStudentId().equals(participant.getStudentId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private record MemberValidationResult(Event event, User user, ActionResponse error) {
    }
}
