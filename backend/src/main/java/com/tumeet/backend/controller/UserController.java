package com.tumeet.backend.controller;

import com.tumeet.backend.dto.ActionResponse;
import com.tumeet.backend.dto.UpdateProfileImageRequest;
import com.tumeet.backend.dto.UpdateUsernameRequest;
import com.tumeet.backend.model.User;
import com.tumeet.backend.repository.UserRepository;
import com.tumeet.backend.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;
    private final EventService eventService;

    public UserController(UserRepository userRepository) {
        this(userRepository, null);
    }

    @Autowired
    public UserController(UserRepository userRepository, EventService eventService) {
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    @PutMapping("/update-username")
    public ResponseEntity<ActionResponse> updateUsername(@RequestBody UpdateUsernameRequest request) {
        if (request == null || request.getStudentId() == null || request.getStudentId().isBlank()) {
            return ResponseEntity.badRequest().body(new ActionResponse(false, "กรุณาระบุรหัสนักศึกษา"));
        }

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body(new ActionResponse(false, "กรุณากรอกชื่อผู้ใช้"));
        }

        Optional<User> userOpt = userRepository.findByStudentId(request.getStudentId().trim());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new ActionResponse(false, "ไม่พบข้อมูลผู้ใช้สำหรับรหัส: " + request.getStudentId()));
        }

        User user = userOpt.get();
        user.setUsername(request.getUsername().trim());
        userRepository.save(user);

        return ResponseEntity.ok(new ActionResponse(true, "อัปเดตชื่อผู้ใช้สำเร็จ", user));
    }

    @PutMapping("/update-profile-image")
    public ResponseEntity<ActionResponse> updateProfileImage(@RequestBody UpdateProfileImageRequest request) {
        if (request == null || request.getStudentId() == null || request.getStudentId().isBlank()) {
            return ResponseEntity.badRequest().body(new ActionResponse(false, "กรุณาระบุรหัสนักศึกษา"));
        }

        if (request.getProfileImage() == null || request.getProfileImage().isBlank()) {
            return ResponseEntity.badRequest().body(new ActionResponse(false, "กรุณาระบุรูปโปรไฟล์"));
        }

        if (!request.getProfileImage().startsWith("data:image/")) {
            return ResponseEntity.badRequest().body(new ActionResponse(false, "รูปโปรไฟล์ต้องเป็น Base64 Data URL ที่ขึ้นต้นด้วย data:image/"));
        }

        if (request.getProfileImage().length() > 7_000_000) {
            return ResponseEntity.badRequest().body(new ActionResponse(false, "รูปโปรไฟล์มีขนาดใหญ่เกินไป (จำกัด ~5MB)"));
        }

        Optional<User> userOpt = userRepository.findByStudentId(request.getStudentId().trim());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new ActionResponse(false, "ไม่พบข้อมูลผู้ใช้สำหรับรหัส: " + request.getStudentId()));
        }

        User user = userOpt.get();
        user.setProfileImage(request.getProfileImage());
        userRepository.save(user);

        return ResponseEntity.ok(new ActionResponse(true, "อัปเดตรูปโปรไฟล์สำเร็จ", user));
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<ActionResponse> getUserProfile(@PathVariable String studentId) {
        if (studentId == null || studentId.isBlank()) {
            return ResponseEntity.badRequest().body(new ActionResponse(false, "กรุณาระบุรหัสนักศึกษา"));
        }

        if (eventService != null) {
            eventService.syncAndCompleteExpiredEvents();
        }

        Optional<User> userOpt = userRepository.findByStudentId(studentId.trim());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new ActionResponse(false, "ไม่พบข้อมูลผู้ใช้สำหรับรหัส: " + studentId));
        }

        return ResponseEntity.ok(new ActionResponse(true, "ดึงข้อมูลผู้ใช้สำเร็จ", userOpt.get()));
    }
}
