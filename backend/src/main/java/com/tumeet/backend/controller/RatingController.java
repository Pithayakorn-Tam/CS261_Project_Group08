package com.tumeet.backend.controller;

import com.tumeet.backend.dto.ActionResponse;
import com.tumeet.backend.dto.RatingRequest;
import com.tumeet.backend.service.RatingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    /**
     * ดึงรายชื่อสมาชิกคนอื่นๆ ในกิจกรรม พร้อมบอกว่าประเมินไปแล้วหรือยัง
     * GET /api/events/{eventId}/ratings/members?raterStudentId={studentId}
     */
    @GetMapping("/{eventId}/ratings/members")
    public ResponseEntity<ActionResponse> getRatingMembers(
            @PathVariable Long eventId,
            @RequestParam String raterStudentId) {

        if (raterStudentId == null || raterStudentId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ActionResponse(false, "กรุณาระบุรหัสนักศึกษาของผู้ประเมิน"));
        }

        ActionResponse response = ratingService.getRatingMembers(eventId, raterStudentId.trim());
        return response.isStatus()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    /**
     * ตรวจสอบสถานะการประเมินกิจกรรมของผู้ใช้
     * GET /api/events/{eventId}/ratings/status?studentId={studentId}
     */
    @GetMapping("/{eventId}/ratings/status")
    public ResponseEntity<ActionResponse> getEventReviewStatus(
            @PathVariable Long eventId,
            @RequestParam String studentId) {

        if (studentId == null || studentId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ActionResponse(false, "กรุณาระบุรหัสนักศึกษา"));
        }

        ActionResponse response = ratingService.getEventReviewStatus(eventId, studentId.trim());
        return ResponseEntity.ok(response);
    }

    /**
     * ดึงรายการรหัสกิจกรรมทั้งหมดที่ผู้ใช้ประเมินไปแล้ว
     * GET /api/events/ratings/reviewed?studentId={studentId}
     */
    @GetMapping("/ratings/reviewed")
    public ResponseEntity<ActionResponse> getReviewedEventIds(@RequestParam String studentId) {
        if (studentId == null || studentId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ActionResponse(false, "กรุณาระบุรหัสนักศึกษา"));
        }

        ActionResponse response = ratingService.getReviewedEventIds(studentId.trim());
        return ResponseEntity.ok(response);
    }

    /**
     * ส่งคะแนนประเมิน
     * POST /api/events/{eventId}/ratings
     */
    @PostMapping("/{eventId}/ratings")
    public ResponseEntity<ActionResponse> submitRating(
            @PathVariable Long eventId,
            @RequestBody RatingRequest request) {

        if (request == null) {
            return ResponseEntity.badRequest()
                    .body(new ActionResponse(false, "กรุณาระบุข้อมูลการประเมิน"));
        }

        if (request.getRaterStudentId() == null || request.getRaterStudentId().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ActionResponse(false, "กรุณาระบุรหัสนักศึกษาของผู้ประเมิน"));
        }

        boolean isFormReview = request.getQ1Score() != null
                || request.getQ2Score() != null
                || request.getQ3Score() != null
                || request.getQ4Score() != null;

        if (!isFormReview && (request.getRateeStudentId() == null || request.getRateeStudentId().isBlank())) {
            return ResponseEntity.badRequest()
                    .body(new ActionResponse(false, "กรุณาระบุรหัสนักศึกษาของผู้ถูกประเมิน"));
        }

        ActionResponse response = ratingService.submitRating(eventId, request);
        return response.isStatus()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }
}
