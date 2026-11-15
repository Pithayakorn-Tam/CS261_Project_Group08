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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private EventRatingRepository eventRatingRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RatingService ratingService;

    private User creator;
    private User participant1;
    private User participant2;
    private Event completedEvent;

    @BeforeEach
    void setUp() {
        creator = new User();
        creator.setId(1L);
        creator.setStudentId("6800000001");
        creator.setUsername("ผู้สร้าง");
        creator.setDisplayname_th("ผู้สร้างทดสอบ");
        creator.setEmail("creator@dome.tu.ac.th");
        creator.setAverageRating(0.0);
        creator.setRatingCount(0);

        participant1 = new User();
        participant1.setId(2L);
        participant1.setStudentId("6800000002");
        participant1.setUsername("สมาชิก1");
        participant1.setDisplayname_th("สมาชิกทดสอบ 1");
        participant1.setEmail("p1@dome.tu.ac.th");
        participant1.setAverageRating(0.0);
        participant1.setRatingCount(0);

        participant2 = new User();
        participant2.setId(3L);
        participant2.setStudentId("6800000003");
        participant2.setUsername("สมาชิก2");
        participant2.setDisplayname_th("สมาชิกทดสอบ 2");
        participant2.setEmail("p2@dome.tu.ac.th");
        participant2.setAverageRating(0.0);
        participant2.setRatingCount(0);

        completedEvent = new Event();
        completedEvent.setId(1L);
        completedEvent.setEventName("ตีแบดมินตัน");
        completedEvent.setEventType("sport");
        completedEvent.setEventSize(5);
        completedEvent.setEventDate(LocalDate.of(2026, 11, 1));
        completedEvent.setEventTime("18:00");
        completedEvent.setStatus("CLOSED");
        completedEvent.setLifecycleStatus("COMPLETED");
        completedEvent.setCreator(creator);
        completedEvent.setCurrentParticipants(3);

        Set<User> participants = new HashSet<>();
        participants.add(participant1);
        participants.add(participant2);
        completedEvent.setParticipants(participants);
    }

    // ===== submitRating Tests =====

    @Test
    @DisplayName("ประเมินคะแนนสำเร็จ - บันทึก rating และอัปเดต averageRating ของ ratee")
    void submitRating_Success() {
        RatingRequest request = new RatingRequest("6800000001", "6800000002", 5);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(completedEvent));
        when(userRepository.findByStudentId("6800000001")).thenReturn(Optional.of(creator));
        when(userRepository.findByStudentId("6800000002")).thenReturn(Optional.of(participant1));
        when(eventRatingRepository.existsByEventIdAndRaterIdAndRateeId(1L, 1L, 2L)).thenReturn(false);
        when(eventRatingRepository.save(any(EventRating.class))).thenAnswer(i -> i.getArgument(0));
        when(eventRatingRepository.countByRateeId(2L)).thenReturn(1);
        when(eventRatingRepository.getAverageScoreByRateeId(2L)).thenReturn(5.0);

        ActionResponse response = ratingService.submitRating(1L, request);

        assertTrue(response.isStatus());
        assertEquals("บันทึกผลการประเมินสำเร็จ", response.getMessage());
        verify(eventRatingRepository).save(any(EventRating.class));
        verify(userRepository).save(participant1);
    }

    @Test
    @DisplayName("ประเมินไม่ได้ - กิจกรรมยังไม่จบ (UPCOMING)")
    void submitRating_EventNotCompleted() {
        completedEvent.setLifecycleStatus("UPCOMING");
        RatingRequest request = new RatingRequest("6800000001", "6800000002", 5);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(completedEvent));

        ActionResponse response = ratingService.submitRating(1L, request);

        assertFalse(response.isStatus());
        assertEquals("ไม่สามารถประเมินได้เนื่องจากกิจกรรมนี้ยังไม่สิ้นสุด", response.getMessage());
        verify(eventRatingRepository, never()).save(any());
    }

    @Test
    @DisplayName("ประเมินไม่ได้ - คะแนนไม่อยู่ในช่วง 1-5")
    void submitRating_InvalidScore() {
        RatingRequest request = new RatingRequest("6800000001", "6800000002", 6);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(completedEvent));

        ActionResponse response = ratingService.submitRating(1L, request);

        assertFalse(response.isStatus());
        assertEquals("คะแนนต้องอยู่ระหว่าง 1 ถึง 5", response.getMessage());
        verify(eventRatingRepository, never()).save(any());
    }

    @Test
    @DisplayName("ประเมินไม่ได้ - ประเมินตัวเอง")
    void submitRating_SelfRating() {
        RatingRequest request = new RatingRequest("6800000001", "6800000001", 5);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(completedEvent));
        when(userRepository.findByStudentId("6800000001")).thenReturn(Optional.of(creator));

        ActionResponse response = ratingService.submitRating(1L, request);

        assertFalse(response.isStatus());
        assertEquals("ไม่สามารถประเมินตัวเองได้", response.getMessage());
        verify(eventRatingRepository, never()).save(any());
    }

    @Test
    @DisplayName("ประเมินไม่ได้ - ประเมินซ้ำในกิจกรรมเดิม")
    void submitRating_DuplicateRating() {
        RatingRequest request = new RatingRequest("6800000001", "6800000002", 4);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(completedEvent));
        when(userRepository.findByStudentId("6800000001")).thenReturn(Optional.of(creator));
        when(userRepository.findByStudentId("6800000002")).thenReturn(Optional.of(participant1));
        when(eventRatingRepository.existsByEventIdAndRaterIdAndRateeId(1L, 1L, 2L)).thenReturn(true);

        ActionResponse response = ratingService.submitRating(1L, request);

        assertFalse(response.isStatus());
        assertEquals("คุณได้ประเมินคนนี้ในกิจกรรมนี้ไปแล้ว", response.getMessage());
        verify(eventRatingRepository, never()).save(any());
    }

    @Test
    @DisplayName("ประเมินไม่ได้ - ผู้ประเมินไม่ใช่สมาชิกของกิจกรรม")
    void submitRating_RaterNotMember() {
        User outsider = new User();
        outsider.setId(99L);
        outsider.setStudentId("6800000099");

        RatingRequest request = new RatingRequest("6800000099", "6800000002", 5);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(completedEvent));
        when(userRepository.findByStudentId("6800000099")).thenReturn(Optional.of(outsider));
        when(userRepository.findByStudentId("6800000002")).thenReturn(Optional.of(participant1));

        ActionResponse response = ratingService.submitRating(1L, request);

        assertFalse(response.isStatus());
        assertEquals("ผู้ประเมินไม่ได้เป็นสมาชิกของกิจกรรมนี้", response.getMessage());
        verify(eventRatingRepository, never()).save(any());
    }

    @Test
    @DisplayName("ประเมินไม่ได้ - ไม่พบกิจกรรม")
    void submitRating_EventNotFound() {
        RatingRequest request = new RatingRequest("6800000001", "6800000002", 5);

        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        ActionResponse response = ratingService.submitRating(999L, request);

        assertFalse(response.isStatus());
        assertEquals("ไม่พบกิจกรรมรหัส: 999", response.getMessage());
    }

    // ===== getRatingMembers Tests =====

    @Test
    @DisplayName("ดึงรายชื่อสมาชิกสำเร็จ - แสดงคนอื่นยกเว้นตัวเอง")
    void getRatingMembers_Success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(completedEvent));
        when(userRepository.findByStudentId("6800000001")).thenReturn(Optional.of(creator));
        when(eventRatingRepository.findByEventIdAndRaterId(1L, 1L)).thenReturn(List.of());

        ActionResponse response = ratingService.getRatingMembers(1L, "6800000001");

        assertTrue(response.isStatus());
        assertEquals("ดึงรายชื่อสมาชิกสำเร็จ", response.getMessage());

        @SuppressWarnings("unchecked")
        List<RatingMemberDto> members = (List<RatingMemberDto>) response.getData();
        assertEquals(2, members.size()); // participant1 + participant2 (ไม่มี creator ตัวเอง)
    }

    @Test
    @DisplayName("ดึงรายชื่อสมาชิก - แสดงสถานะ alreadyRated ถูกต้อง")
    void getRatingMembers_ShowsAlreadyRated() {
        EventRating existingRating = new EventRating(completedEvent, creator, participant1, 5);
        existingRating.setId(1L);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(completedEvent));
        when(userRepository.findByStudentId("6800000001")).thenReturn(Optional.of(creator));
        when(eventRatingRepository.findByEventIdAndRaterId(1L, 1L)).thenReturn(List.of(existingRating));

        ActionResponse response = ratingService.getRatingMembers(1L, "6800000001");

        assertTrue(response.isStatus());

        @SuppressWarnings("unchecked")
        List<RatingMemberDto> members = (List<RatingMemberDto>) response.getData();

        // หาสมาชิกที่ประเมินแล้ว
        RatingMemberDto ratedMember = members.stream()
                .filter(m -> "6800000002".equals(m.getStudentId()))
                .findFirst().orElse(null);

        assertNotNull(ratedMember);
        assertTrue(ratedMember.isAlreadyRated());
        assertEquals(5, ratedMember.getGivenScore());
    }

    // ===== submitEventReview / status / reviewed Tests (Form Review.html) =====

    @Test
    @DisplayName("ประเมินแบบฟอร์ม 4 หัวข้อสำเร็จ - บันทึก 1 แถวให้ผู้สร้างและอัปเดตคะแนนของผู้สร้าง")
    void submitEventReview_Success_FourCriteriaAndComment() {
        RatingRequest request = new RatingRequest();
        request.setRaterStudentId("6800000002");
        request.setRole("participant");
        request.setQ1Score(5);
        request.setQ2Score(4);
        request.setQ3Score(5);
        request.setQ4Score(4);
        request.setComment("  กิจกรรมสนุกมาก  ");

        when(eventRepository.findById(1L)).thenReturn(Optional.of(completedEvent));
        when(userRepository.findByStudentId("6800000002")).thenReturn(Optional.of(participant1));
        when(eventRatingRepository.findByEventIdAndRaterId(1L, 2L)).thenReturn(List.of());
        when(eventRatingRepository.save(any(EventRating.class))).thenAnswer(i -> i.getArgument(0));
        when(eventRatingRepository.countByRateeId(1L)).thenReturn(1);
        when(eventRatingRepository.getAverageScoreByRateeId(1L)).thenReturn(5.0);

        ActionResponse response = ratingService.submitRating(1L, request);

        assertTrue(response.isStatus());
        assertEquals("บันทึกผลการประเมินสำเร็จ", response.getMessage());
        verify(eventRatingRepository, times(1)).save(any(EventRating.class));
        verify(userRepository).save(creator);
        verify(userRepository, never()).save(participant2);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getData();
        assertEquals(1L, data.get("eventId"));
        assertEquals("6800000002", data.get("raterStudentId"));
        assertEquals(5, data.get("score"));
    }

    @Test
    @DisplayName("ประเมินแบบฟอร์มไม่ได้ - ผู้สร้างไม่สามารถประเมินกิจกรรมของตัวเองได้")
    void submitEventReview_CreatorCannotReviewOwnEvent() {
        RatingRequest request = new RatingRequest();
        request.setRaterStudentId("6800000001");
        request.setRole("creator");
        request.setQ1Score(5);
        request.setQ2Score(5);
        request.setQ3Score(5);
        request.setQ4Score(5);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(completedEvent));
        when(userRepository.findByStudentId("6800000001")).thenReturn(Optional.of(creator));

        ActionResponse response = ratingService.submitRating(1L, request);

        assertFalse(response.isStatus());
        assertEquals("ผู้สร้างกิจกรรมไม่สามารถประเมินกิจกรรมของตัวเองได้", response.getMessage());
        verify(eventRatingRepository, never()).save(any());
    }

    @Test
    @DisplayName("ประเมินแบบฟอร์มไม่ได้ - เคยประเมินกิจกรรมนี้ไปแล้ว")
    void submitEventReview_DuplicateEventReview() {
        EventRating existing = new EventRating(completedEvent, participant1, creator, 4);
        RatingRequest request = new RatingRequest();
        request.setRaterStudentId("6800000002");
        request.setQ1Score(4);
        request.setQ2Score(4);
        request.setQ3Score(4);
        request.setQ4Score(4);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(completedEvent));
        when(userRepository.findByStudentId("6800000002")).thenReturn(Optional.of(participant1));
        when(eventRatingRepository.findByEventIdAndRaterId(1L, 2L)).thenReturn(List.of(existing));

        ActionResponse response = ratingService.submitRating(1L, request);

        assertFalse(response.isStatus());
        assertEquals("คุณได้ประเมินกิจกรรมนี้ไปแล้ว", response.getMessage());
        verify(eventRatingRepository, never()).save(any());
    }

    @Test
    @DisplayName("ดึงรายการรหัสกิจกรรมที่ประเมินแล้วสำเร็จ")
    void getReviewedEventIds_ReturnsReviewedList() {
        when(eventRatingRepository.findReviewedEventIdsByRaterStudentId("6800000001"))
                .thenReturn(List.of(1L, 2L));

        ActionResponse response = ratingService.getReviewedEventIds("6800000001");

        assertTrue(response.isStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getData();
        assertEquals(List.of(1L, 2L), data.get("reviewedEventIds"));
    }

    @Test
    @DisplayName("ตรวจสอบสถานะการประเมิน - คืนคะแนน q1-q4 และคอมเมนต์เมื่อประเมินแล้ว")
    void getEventReviewStatus_WhenReviewed_ReturnsScoresAndComment() {
        EventRating existing = new EventRating(completedEvent, creator, participant1, 5);
        existing.setQ1Score(5);
        existing.setQ2Score(4);
        existing.setQ3Score(5);
        existing.setQ4Score(5);
        existing.setComment("ดีมากครับ");

        when(eventRepository.findById(1L)).thenReturn(Optional.of(completedEvent));
        when(userRepository.findByStudentId("6800000001")).thenReturn(Optional.of(creator));
        when(eventRatingRepository.findByEventIdAndRaterId(1L, 1L)).thenReturn(List.of(existing));

        ActionResponse response = ratingService.getEventReviewStatus(1L, "6800000001");

        assertTrue(response.isStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getData();
        assertEquals(true, data.get("reviewed"));
        assertEquals(5, data.get("q1Score"));
        assertEquals(4, data.get("q2Score"));
        assertEquals("ดีมากครับ", data.get("comment"));
    }
}
