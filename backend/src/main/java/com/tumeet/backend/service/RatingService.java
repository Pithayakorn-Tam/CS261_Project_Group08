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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class RatingService {

    private final EventRatingRepository eventRatingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public RatingService(EventRatingRepository eventRatingRepository,
                         EventRepository eventRepository,
                         UserRepository userRepository) {
        this(eventRatingRepository, eventRepository, userRepository, null);
    }

    @Autowired
    public RatingService(EventRatingRepository eventRatingRepository,
                         EventRepository eventRepository,
                         UserRepository userRepository,
                         NotificationService notificationService) {
        this.eventRatingRepository = eventRatingRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
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
        if (!isEventCompleted(event)) {
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
     * บันทึกผลการประเมินคะแนน (รองรับทั้งแบบรายคนและแบบฟอร์ม 4 หัวข้อ + ความคิดเห็น)
     */
    @Transactional
    public ActionResponse submitRating(Long eventId, RatingRequest request) {
        if (request == null) {
            return new ActionResponse(false, "กรุณาระบุข้อมูลการประเมิน");
        }
        if (request.getRateeStudentId() == null || request.getRateeStudentId().isBlank()) {
            return submitEventReview(eventId, request);
        }

        // 1. ตรวจสอบกิจกรรม
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return new ActionResponse(false, "ไม่พบกิจกรรมรหัส: " + eventId);
        }
        Event event = eventOpt.get();

        // 2. ตรวจสอบว่ากิจกรรม COMPLETED เท่านั้น
        if (!isEventCompleted(event)) {
            return new ActionResponse(false, "ไม่สามารถประเมินได้เนื่องจากกิจกรรมนี้ยังไม่สิ้นสุด");
        }

        // 3. ตรวจสอบคะแนน 1-5
        if (!isValidScore(request.getScore())) {
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
        updateUserRatingStats(ratee);

        return new ActionResponse(true, "บันทึกผลการประเมินสำเร็จ", rating);
    }

    /**
     * บันทึกผลการประเมินกิจกรรมจากแบบฟอร์ม 4 หัวข้อ + ความคิดเห็นเพิ่มเติม (1 ครั้งต่อกิจกรรม)
     */
    @Transactional
    public ActionResponse submitEventReview(Long eventId, RatingRequest request) {
        if (request == null || request.getRaterStudentId() == null || request.getRaterStudentId().isBlank()) {
            return new ActionResponse(false, "กรุณาระบุรหัสนักศึกษาของผู้ประเมิน");
        }

        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return new ActionResponse(false, "ไม่พบกิจกรรมรหัส: " + eventId);
        }
        Event event = eventOpt.get();

        if (!isEventCompleted(event)) {
            return new ActionResponse(false, "ไม่สามารถประเมินได้เนื่องจากกิจกรรมนี้ยังไม่สิ้นสุด");
        }

        String raterStudentId = request.getRaterStudentId().trim();
        Optional<User> raterOpt = userRepository.findByStudentId(raterStudentId);
        if (raterOpt.isEmpty()) {
            return new ActionResponse(false, "ไม่พบข้อมูลผู้ประเมินสำหรับรหัส: " + raterStudentId);
        }
        User rater = raterOpt.get();

        if (!isMemberOfEvent(event, rater)) {
            return new ActionResponse(false, "ผู้ประเมินไม่ได้เป็นสมาชิกของกิจกรรมนี้");
        }

        User creator = event.getCreator();
        if (creator == null) {
            return new ActionResponse(false, "ไม่พบผู้สร้างกิจกรรมสำหรับรหัสกิจกรรม: " + eventId);
        }
        if (creator.getId().equals(rater.getId())) {
            return new ActionResponse(false, "ผู้สร้างกิจกรรมไม่สามารถประเมินกิจกรรมของตัวเองได้");
        }

        Integer q1 = request.getQ1Score();
        Integer q2 = request.getQ2Score();
        Integer q3 = request.getQ3Score();
        Integer q4 = request.getQ4Score();
        if (!isValidScore(q1) || !isValidScore(q2) || !isValidScore(q3) || !isValidScore(q4)) {
            return new ActionResponse(false, "คะแนนต้องอยู่ระหว่าง 1 ถึง 5");
        }

        if (!eventRatingRepository.findByEventIdAndRaterId(eventId, rater.getId()).isEmpty()) {
            return new ActionResponse(false, "คุณได้ประเมินกิจกรรมนี้ไปแล้ว");
        }

        int avgScore = (int) Math.round((q1 + q2 + q3 + q4) / 4.0);
        String trimmedComment = request.getComment() != null ? request.getComment().trim() : null;
        if (trimmedComment != null && trimmedComment.length() > 200) {
            trimmedComment = trimmedComment.substring(0, 200);
        }

        EventRating rating = new EventRating(event, rater, creator, avgScore);
        rating.setQ1Score(q1);
        rating.setQ2Score(q2);
        rating.setQ3Score(q3);
        rating.setQ4Score(q4);
        rating.setComment(trimmedComment);
        eventRatingRepository.save(rating);

        updateUserRatingStats(creator);

        int prevPoints = rater.getEvaluatedCount();
        int newPoints = prevPoints + 1;
        rater.setEvaluatedCount(newPoints);
        userRepository.save(rater);
        if (notificationService != null) {
            notificationService.notifyBadgeUpgradedIfEligible(rater, event, prevPoints, newPoints);
        }

        Map<String, Object> resultData = new LinkedHashMap<>();
        resultData.put("eventId", eventId);
        resultData.put("raterStudentId", rater.getStudentId());
        resultData.put("score", avgScore);
        resultData.put("evaluatedCount", newPoints);
        resultData.put("badge", rater.getBadge());
        return new ActionResponse(true, "บันทึกผลการประเมินสำเร็จ", resultData);
    }

    /**
     * ตรวจสอบสถานะการประเมินกิจกรรมของผู้ใช้สำหรับหน้า Review.html
     */
    public ActionResponse getEventReviewStatus(Long eventId, String studentId) {
        Map<String, Object> notReviewedData = new LinkedHashMap<>();
        notReviewedData.put("eventId", eventId);
        notReviewedData.put("reviewed", false);
        notReviewedData.put("isCreator", false);

        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (studentId == null || studentId.isBlank() || eventOpt.isEmpty()) {
            return new ActionResponse(true, "ยังไม่ได้ประเมินกิจกรรมนี้", notReviewedData);
        }

        Optional<User> raterOpt = userRepository.findByStudentId(studentId.trim());
        if (raterOpt.isEmpty()) {
            return new ActionResponse(true, "ยังไม่ได้ประเมินกิจกรรมนี้", notReviewedData);
        }

        User rater = raterOpt.get();
        Event event = eventOpt.get();
        boolean isCreator = event.getCreator() != null && event.getCreator().getId().equals(rater.getId());
        notReviewedData.put("isCreator", isCreator);

        List<EventRating> existing = eventRatingRepository.findByEventIdAndRaterId(eventId, rater.getId());
        if (existing == null || existing.isEmpty()) {
            return new ActionResponse(true, "ยังไม่ได้ประเมินกิจกรรมนี้", notReviewedData);
        }

        EventRating r = existing.get(0);
        int baseScore = r.getScore() != null ? r.getScore() : 0;
        Map<String, Object> reviewedData = new LinkedHashMap<>();
        reviewedData.put("eventId", eventId);
        reviewedData.put("reviewed", true);
        reviewedData.put("isCreator", isCreator);
        reviewedData.put("score", baseScore);
        reviewedData.put("q1Score", r.getQ1Score() != null ? r.getQ1Score() : baseScore);
        reviewedData.put("q2Score", r.getQ2Score() != null ? r.getQ2Score() : baseScore);
        reviewedData.put("q3Score", r.getQ3Score() != null ? r.getQ3Score() : baseScore);
        reviewedData.put("q4Score", r.getQ4Score() != null ? r.getQ4Score() : baseScore);
        reviewedData.put("comment", r.getComment() != null ? r.getComment() : "");
        return new ActionResponse(true, "ประเมินกิจกรรมนี้แล้ว", reviewedData);
    }

    /**
     * ดึงรายการรหัสกิจกรรมทั้งหมดที่ผู้ใช้คนนี้ประเมินไปแล้ว
     */
    public ActionResponse getReviewedEventIds(String studentId) {
        List<Long> ids = (studentId == null || studentId.isBlank())
                ? List.of()
                : eventRatingRepository.findReviewedEventIdsByRaterStudentId(studentId.trim());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reviewedEventIds", ids != null ? ids : List.of());
        return new ActionResponse(true, "ดึงรายการกิจกรรมที่ประเมินแล้วสำเร็จ", data);
    }

    private boolean isEventCompleted(Event event) {
        if ("COMPLETED".equalsIgnoreCase(event.getLifecycleStatus())) {
            return true;
        }
        return (event.getLifecycleStatus() == null || event.getLifecycleStatus().isBlank())
                && "CLOSED".equalsIgnoreCase(event.getStatus());
    }

    private boolean isValidScore(Integer score) {
        return score != null && score >= 1 && score <= 5;
    }

    private void updateUserRatingStats(User ratee) {
        int newRatingCount = eventRatingRepository.countByRateeId(ratee.getId());
        double rawAverage = eventRatingRepository.getAverageScoreByRateeId(ratee.getId());
        double roundedAverage = BigDecimal.valueOf(rawAverage)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        ratee.setRatingCount(newRatingCount);
        ratee.setAverageRating(roundedAverage);
        userRepository.save(ratee);
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
