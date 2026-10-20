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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EventParticipantService {

    private final EventParticipantRepository eventParticipantRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Autowired
    public EventParticipantService(EventParticipantRepository eventParticipantRepository,
                                   EventRepository eventRepository,
                                   UserRepository userRepository) {
        this.eventParticipantRepository = eventParticipantRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public ActionResponse joinEvent(JoinEventRequest request) {
        if (request == null || request.getEventId() == null || request.getStudentId() == null || request.getStudentId().isBlank()) {
            return new ActionResponse(false, "ข้อมูลคำขอไม่ถูกต้อง กรุณาระบุรหัสกิจกรรมและรหัสนักศึกษา");
        }

        String studentId = request.getStudentId().trim();

        // 1. ตรวจสอบข้อมูลผู้ใช้
        Optional<User> userOptional = userRepository.findByStudentId(studentId);
        if (userOptional.isEmpty()) {
            return new ActionResponse(false, "ไม่พบข้อมูลผู้ใช้งานรหัสนักศึกษา: " + studentId);
        }
        User user = userOptional.get();

        // 2. ตรวจสอบข้อมูลกิจกรรม
        Optional<Event> eventOptional = eventRepository.findById(request.getEventId());
        if (eventOptional.isEmpty()) {
            return new ActionResponse(false, "ไม่พบข้อมูลกิจกรรม ID: " + request.getEventId());
        }
        Event event = eventOptional.get();

        // 3. ตรวจสอบสถานะกิจกรรม
        if ("CLOSED".equalsIgnoreCase(event.getStatus())) {
            return new ActionResponse(false, "กิจกรรมนี้ปิดรับสมัครแล้ว");
        }
        if ("FULL".equalsIgnoreCase(event.getStatus()) || event.getCurrentParticipants() >= event.getEventSize()) {
            event.setStatus("FULL");
            eventRepository.save(event);
            return new ActionResponse(false, "กิจกรรมนี้มีผู้เข้าร่วมเต็มแล้ว");
        }

        // 4. ตรวจสอบการเข้าร่วมซ้ำ
        if (eventParticipantRepository.existsByEventIdAndUserStudentId(event.getId(), user.getStudentId())) {
            return new ActionResponse(false, "คุณได้เข้าร่วมกิจกรรมนี้ไปแล้ว");
        }

        // 5. บันทึกข้อมูลการเข้าร่วม
        EventParticipant participant = new EventParticipant(event, user);
        eventParticipantRepository.save(participant);

        // 6. อัปเดตจำนวนคนและสถานะของกิจกรรม
        int updatedCount = event.getCurrentParticipants() + 1;
        event.setCurrentParticipants(updatedCount);
        if (updatedCount >= event.getEventSize()) {
            event.setStatus("FULL");
        }
        eventRepository.save(event);

        return new ActionResponse(true, "เข้าร่วมกิจกรรมสำเร็จ", event);
    }

    public ActionResponse leaveEvent(LeaveEventRequest request) {
        if (request == null || request.getEventId() == null || request.getStudentId() == null || request.getStudentId().isBlank()) {
            return new ActionResponse(false, "ข้อมูลคำขอไม่ถูกต้อง กรุณาระบุรหัสกิจกรรมและรหัสนักศึกษา");
        }

        String studentId = request.getStudentId().trim();

        // 1. ตรวจสอบข้อมูลผู้ใช้
        Optional<User> userOptional = userRepository.findByStudentId(studentId);
        if (userOptional.isEmpty()) {
            return new ActionResponse(false, "ไม่พบข้อมูลผู้ใช้งานรหัสนักศึกษา: " + studentId);
        }
        User user = userOptional.get();

        // 2. ตรวจสอบข้อมูลกิจกรรม
        Optional<Event> eventOptional = eventRepository.findById(request.getEventId());
        if (eventOptional.isEmpty()) {
            return new ActionResponse(false, "ไม่พบข้อมูลกิจกรรม ID: " + request.getEventId());
        }
        Event event = eventOptional.get();

        // 3. ป้องกันไม่ให้ผู้สร้างกิจกรรมกดออกจากกิจกรรมตนเอง
        if (event.getCreator() != null && user.getStudentId().equals(event.getCreator().getStudentId())) {
            return new ActionResponse(false, "ผู้สร้างกิจกรรมไม่สามารถยกเลิกการเข้าร่วมได้");
        }

        // 4. ตรวจสอบประวัติการเข้าร่วม
        Optional<EventParticipant> participantOptional =
                eventParticipantRepository.findByEventIdAndUserStudentId(event.getId(), user.getStudentId());
        if (participantOptional.isEmpty()) {
            return new ActionResponse(false, "คุณไม่ได้เป็นผู้เข้าร่วมกิจกรรมนี้");
        }

        // 5. ลบข้อมูลการเข้าร่วม
        eventParticipantRepository.delete(participantOptional.get());

        // 6. ลดจำนวนคนและปรับสถานะกิจกรรมกลับเป็น OPEN หากมีที่ว่าง
        int updatedCount = Math.max(0, event.getCurrentParticipants() - 1);
        event.setCurrentParticipants(updatedCount);

        if ("FULL".equalsIgnoreCase(event.getStatus()) && updatedCount < event.getEventSize()) {
            event.setStatus("OPEN");
        }
        eventRepository.save(event);

        return new ActionResponse(true, "ยกเลิกการเข้าร่วมกิจกรรมสำเร็จ", event);
    }

    @Transactional(readOnly = true)
    public List<EventParticipant> getParticipantsByEventId(Long eventId) {
        return eventParticipantRepository.findByEventId(eventId);
    }
}