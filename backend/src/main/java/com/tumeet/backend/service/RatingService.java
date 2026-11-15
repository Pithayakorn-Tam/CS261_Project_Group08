package com.tumeet.backend.service;

import com.tumeet.backend.dto.ActionResponse;
import com.tumeet.backend.dto.RatingMemberDto;
import com.tumeet.backend.dto.RatingRequest;
import com.tumeet.backend.model.Event;
import com.tumeet.backend.model.EventRating;
import com.tumeet.backend.model.User;
import com.tumeet.backend.repository.EventRatingRepository;
import com.tumeet.backend.repository.EventRepository;
import com.tumeet.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RatingService {

    private final EventRatingRepository eventRatingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public RatingService(EventRatingRepository eventRatingRepository,
                         EventRepository eventRepository,
                         UserRepository userRepository) {
        this.eventRatingRepository = eventRatingRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    /**
     * ดึงรายชื่อสมาชิกคนอื่นๆ ในกิจกรรม พร้อมบอกว่าประเมินไปแล้วหรือยัง
     */
    public ActionResponse getRatingMembers(Long eventId, String raterStudentId) {
        // ตรวจสอบกิจกรรม
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return new ActionResponse(false, "ไม่พบกิจกรรมรหัส: " + eventId);
        }
        Event event = eventOpt.get();

        // ตรวจสอบว่ากิจกรรมจบแล้ว
        if (!"COMPLETED".equalsIgnoreCase(event.getLifecycleStatus())) {
            return new ActionResponse(false, "ไม่สามารถประเมินได้เนื่องจากกิจกรรมนี้ยังไม่สิ้นสุด");
        }

        // ตรวจสอบผู้ประเมิน
        Optional<User> raterOpt = userRepository.findByStudentId(raterStudentId);
        if (raterOpt.isEmpty()) {
            return new ActionResponse(false, "ไม่พบข้อมูลผู้ใช้สำหรับรหัส: " + raterStudentId);
        }
        User rater = raterOpt.get();

        // ตรวจสอบว่า rater เป็นสมาชิกของกิจกรรม
        if (!isMemberOfEvent(event, rater)) {
            return new ActionResponse(false, "คุณไม่ได้เป็นสมาชิกของกิจกรรมนี้");
        }

        // ดึง rating ที่ rater เคยให้ไปแล้วในกิจกรรมนี้
        List<EventRating> existingRatings = eventRatingRepository.findByEventIdAndRaterId(eventId, rater.getId());

        // สร้างรายชื่อสมาชิกทั้งหมด (ยกเว้นตัวเอง)
        List<RatingMemberDto> members = new ArrayList<>();

        // เพิ่ม Creator (ถ้าไม่ใช่ตัวเอง)
        User creator = event.getCreator();
        if (creator != null && !creator.getStudentId().equals(raterStudentId)) {
            members.add(buildMemberDto(creator, existingRatings));
        }

        // เพิ่ม Participants (ยกเว้นตัวเอง)
        Set<User> participants = event.getParticipants();
        if (participants != null) {
            for (User participant : participants) {
                if (!participant.getStudentId().equals(raterStudentId)) {
                    members.add(buildMemberDto(participant, existingRatings));
                }
            }
        }

        return new ActionResponse(true, "ดึงรายชื่อสมาชิกสำเร็จ", members);
    }

    /**
     * บันทึกผลการประเมินคะแนน
     */
    @Transactional
    public ActionResponse submitRating(Long eventId, RatingRequest request) {
        // 1. ตรวจสอบกิจกรรม
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return new ActionResponse(false, "ไม่พบกิจกรรมรหัส: " + eventId);
        }
        Event event = eventOpt.get();

        // 2. ตรวจสอบว่ากิจกรรม COMPLETED เท่านั้น
        if (!"COMPLETED".equalsIgnoreCase(event.getLifecycleStatus())) {
            return new ActionResponse(false, "ไม่สามารถประเมินได้เนื่องจากกิจกรรมนี้ยังไม่สิ้นสุด");
        }

        // 3. ตรวจสอบคะแนน 1-5
        if (request.getScore() == null || request.getScore() < 1 || request.getScore() > 5) {
            return new ActionResponse(false, "คะแนนต้องอยู่ระหว่าง 1 ถึง 5");
        }

        // 4. ตรวจสอบ rater
        Optional<User> raterOpt = userRepository.findByStudentId(request.getRaterStudentId());
        if (raterOpt.isEmpty()) {
            return new ActionResponse(false, "ไม่พบข้อมูลผู้ประเมินสำหรับรหัส: " + request.getRaterStudentId());
        }
        User rater = raterOpt.get();

        // 5. ตรวจสอบ ratee
        Optional<User> rateeOpt = userRepository.findByStudentId(request.getRateeStudentId());
        if (rateeOpt.isEmpty()) {
            return new ActionResponse(false, "ไม่พบข้อมูลผู้ถูกประเมินสำหรับรหัส: " + request.getRateeStudentId());
        }
        User ratee = rateeOpt.get();

        // 6. ห้ามประเมินตัวเอง
        if (rater.getStudentId().equals(ratee.getStudentId())) {
            return new ActionResponse(false, "ไม่สามารถประเมินตัวเองได้");
        }

        // 7. ตรวจสอบว่าทั้ง rater และ ratee เป็นสมาชิกของกิจกรรม
        if (!isMemberOfEvent(event, rater)) {
            return new ActionResponse(false, "ผู้ประเมินไม่ได้เป็นสมาชิกของกิจกรรมนี้");
        }
        if (!isMemberOfEvent(event, ratee)) {
            return new ActionResponse(false, "ผู้ถูกประเมินไม่ได้เป็นสมาชิกของกิจกรรมนี้");
        }

        // 8. ตรวจสอบว่าไม่เคยประเมินคนนี้ในกิจกรรมนี้มาก่อน
        if (eventRatingRepository.existsByEventIdAndRaterIdAndRateeId(eventId, rater.getId(), ratee.getId())) {
            return new ActionResponse(false, "คุณได้ประเมินคนนี้ในกิจกรรมนี้ไปแล้ว");
        }

        // 9. บันทึก EventRating
        EventRating rating = new EventRating(event, rater, ratee, request.getScore());
        eventRatingRepository.save(rating);

        // 10. คำนวณค่าเฉลี่ยใหม่จาก DB แล้วอัปเดตลง ratee
        int newRatingCount = eventRatingRepository.countByRateeId(ratee.getId());
        double newAverageRating = eventRatingRepository.getAverageScoreByRateeId(ratee.getId());

        // ปัดทศนิยม 2 ตำแหน่ง
        newAverageRating = BigDecimal.valueOf(newAverageRating)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        ratee.setRatingCount(newRatingCount);
        ratee.setAverageRating(newAverageRating);
        userRepository.save(ratee);

        return new ActionResponse(true, "บันทึกผลการประเมินสำเร็จ", rating);
    }

    /**
     * ตรวจสอบว่าผู้ใช้เป็น creator หรือ participant ของกิจกรรม
     */
    private boolean isMemberOfEvent(Event event, User user) {
        // เป็น creator?
        if (event.getCreator() != null && event.getCreator().getId().equals(user.getId())) {
            return true;
        }
        // เป็น participant?
        if (event.getParticipants() != null) {
            return event.getParticipants().stream()
                    .anyMatch(p -> p.getId().equals(user.getId()));
        }
        return false;
    }

    /**
     * สร้าง RatingMemberDto พร้อมข้อมูลว่าประเมินไปแล้วหรือยัง
     */
    private RatingMemberDto buildMemberDto(User member, List<EventRating> existingRatings) {
        RatingMemberDto dto = new RatingMemberDto();
        dto.setStudentId(member.getStudentId());
        dto.setUsername(member.getUsername());
        dto.setDisplayname_th(member.getDisplayname_th());
        dto.setProfileImage(member.getProfileImage());

        // หาว่าเคยประเมินคนนี้ไปแล้วหรือยัง
        Optional<EventRating> existingRating = existingRatings.stream()
                .filter(r -> r.getRatee().getId().equals(member.getId()))
                .findFirst();

        dto.setAlreadyRated(existingRating.isPresent());
        dto.setGivenScore(existingRating.map(EventRating::getScore).orElse(null));

        return dto;
    }
}
