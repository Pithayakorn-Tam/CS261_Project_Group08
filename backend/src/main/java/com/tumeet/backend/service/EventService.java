package com.tumeet.backend.service;

import com.tumeet.backend.dto.CreateEventRequest;
import com.tumeet.backend.dto.EventDto;
import com.tumeet.backend.dto.EventResponse;
import com.tumeet.backend.model.Event;
import com.tumeet.backend.model.User;
import com.tumeet.backend.repository.EventRepository;
import com.tumeet.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        if (request == null) {
            return EventResponse.error("ข้อมูลคำขอสร้างกิจกรรมไม่ถูกต้อง");
        }

        if (request.getCreatorStudentId() == null || request.getCreatorStudentId().isBlank()) {
            return EventResponse.error("กรุณาระบุรหัสนักศึกษาของผู้สร้างกิจกรรม");
        }

        // 1. ค้นหาผู้สร้างจาก userRepository.findByStudentId
        Optional<User> creatorOpt = userRepository.findByStudentId(request.getCreatorStudentId().trim());
        if (creatorOpt.isEmpty()) {
            return EventResponse.error("ไม่พบข้อมูลผู้ใช้สำหรับรหัสนักศึกษา: " + request.getCreatorStudentId());
        }

        // 2. ตรวจสอบข้อมูลความถูกต้องพื้นฐาน
        if (request.getEventName() == null || request.getEventName().isBlank()) {
            return EventResponse.error("กรุณาระบุชื่อกิจกรรม");
        }

        if (request.getEventType() == null || request.getEventType().isBlank()) {
            return EventResponse.error("กรุณาระบุหมวดหมู่กิจกรรม");
        }

        if (request.getEventSize() == null || request.getEventSize() < 1) {
            return EventResponse.error("จำนวนคนสูงสุด (Event Size) ต้องมากกว่าหรือเท่ากับ 1");
        }

        if (request.getEventDate() == null || request.getEventDate().isBlank()) {
            return EventResponse.error("กรุณาระบุวันที่จัดกิจกรรม");
        }

        LocalDate eventDate;
        try {
            eventDate = LocalDate.parse(request.getEventDate().trim());
        } catch (DateTimeParseException e) {
            return EventResponse.error("รูปแบบวันที่ไม่ถูกต้อง กรุณาใช้วันที่ในรูปแบบ YYYY-MM-DD");
        }

        if (request.getEventTime() == null || request.getEventTime().isBlank()) {
            return EventResponse.error("กรุณาระบุเวลาจัดกิจกรรม");
        }

        // 3. สร้างอ็อบเจกต์ Event และกำหนดค่าความสัมพันธ์ event.setCreator(creator)
        Event event = new Event();
        event.setEventName(request.getEventName().trim());
        event.setEventType(request.getEventType().trim());
        event.setLocation(request.getLocation() != null ? request.getLocation().trim() : null);
        event.setEventSize(request.getEventSize());
        event.setCurrentParticipants(0); // เริ่มต้นที่ 0/eventSize
        event.setEventDate(eventDate);
        event.setEventTime(request.getEventTime().trim());
        event.setStatus("OPEN");
        event.setCreator(creatorOpt.get());

        // 4. บันทึกลงฐานข้อมูล
        Event savedEvent = eventRepository.save(event);

        // 5. แปลงข้อมูลส่งออกเป็น EventResponse
        EventDto eventDto = EventDto.fromEntity(savedEvent);
        return EventResponse.success("สร้างกิจกรรมสำเร็จ", eventDto);
    }

    @Transactional(readOnly = true)
    public EventResponse getAllOpenEvents() {
        List<Event> allEvents = eventRepository.findAllByOrderByIdAsc();
        if (allEvents.isEmpty()) {
            allEvents = eventRepository.findByStatusOrderByIdAsc("OPEN");
        }
        List<EventDto> eventDtos = allEvents.stream()
                .map(EventDto::fromEntity)
                .toList();

        return EventResponse.success("ดึงรายการกิจกรรมที่เปิดรับอยู่สำเร็จ", eventDtos);
    }

    @Transactional(readOnly = true)
    public EventResponse getEventsByCreator(String studentId) {
        if (studentId == null || studentId.isBlank()) {
            return EventResponse.error("กรุณาระบุรหัสนักศึกษา");
        }

        Optional<User> userOpt = userRepository.findByStudentId(studentId.trim());
        if (userOpt.isEmpty()) {
            return EventResponse.error("ไม่พบข้อมูลผู้ใช้สำหรับรหัสนักศึกษา: " + studentId);
        }

        List<Event> userEvents = eventRepository.findByCreator(userOpt.get());
        List<EventDto> eventDtos = userEvents.stream()
                .map(EventDto::fromEntity)
                .toList();

        return EventResponse.success("ดึงรายการกิจกรรมของผู้ใช้สำเร็จ", eventDtos);
    }

    @Transactional(readOnly = true)
    public EventResponse getEventsByParticipant(String studentId) {
        if (studentId == null || studentId.isBlank()) {
            return EventResponse.error("กรุณาระบุรหัสนักศึกษา");
        }

        Optional<User> userOpt = userRepository.findByStudentId(studentId.trim());
        if (userOpt.isEmpty()) {
            return EventResponse.error("ไม่พบข้อมูลผู้ใช้สำหรับรหัสนักศึกษา: " + studentId);
        }

        List<Event> joinedEvents = eventRepository.findByParticipantsContaining(userOpt.get());
        List<EventDto> eventDtos = joinedEvents.stream()
                .map(EventDto::fromEntity)
                .toList();

        return EventResponse.success("ดึงรายการกิจกรรมที่เข้าร่วมสำเร็จ", eventDtos);
    }

    @Transactional
    public EventResponse joinEvent(Long eventId, String studentId) {
        if (eventId == null || studentId == null || studentId.isBlank()) {
            return EventResponse.error("กรุณาระบุรหัสกิจกรรมและรหัสนักศึกษา");
        }

        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return EventResponse.error("ไม่พบกิจกรรมรหัส: " + eventId);
        }

        Optional<User> userOpt = userRepository.findByStudentId(studentId.trim());
        if (userOpt.isEmpty()) {
            return EventResponse.error("ไม่พบข้อมูลผู้ใช้สำหรับรหัสนักศึกษา: " + studentId);
        }

        Event event = eventOpt.get();
        User user = userOpt.get();

        if (event.getParticipants().contains(user)) {
            return EventResponse.error("คุณได้เข้าร่วมกิจกรรมนี้แล้ว");
        }

        if (event.getCurrentParticipants() >= event.getEventSize()) {
            return EventResponse.error("กิจกรรมนี้เต็มแล้ว");
        }

        event.addParticipant(user);
        Event saved = eventRepository.save(event);
        return EventResponse.success("เข้าร่วมกิจกรรมสำเร็จ", EventDto.fromEntity(saved));
    }

    @Transactional
    public EventResponse leaveEvent(Long eventId, String studentId) {
        if (eventId == null || studentId == null || studentId.isBlank()) {
            return EventResponse.error("กรุณาระบุรหัสกิจกรรมและรหัสนักศึกษา");
        }

        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return EventResponse.error("ไม่พบกิจกรรมรหัส: " + eventId);
        }

        Optional<User> userOpt = userRepository.findByStudentId(studentId.trim());
        if (userOpt.isEmpty()) {
            return EventResponse.error("ไม่พบข้อมูลผู้ใช้สำหรับรหัสนักศึกษา: " + studentId);
        }

        Event event = eventOpt.get();
        User user = userOpt.get();

        if (!event.getParticipants().contains(user)) {
            if (event.getCreator() != null && user.getStudentId().equals(event.getCreator().getStudentId())) {
                event.getParticipants().clear();
                eventRepository.delete(event);
                return EventResponse.success("ยกเลิกกิจกรรมสำเร็จ", EventDto.fromEntity(event));
            }
            return EventResponse.error("คุณไม่ได้อยู่ในรายชื่อผู้เข้าร่วมกิจกรรมนี้");
        }

        event.removeParticipant(user);
        Event saved = eventRepository.save(event);
        return EventResponse.success("ยกเลิกการเข้าร่วมกิจกรรมสำเร็จ", EventDto.fromEntity(saved));
    }
}
