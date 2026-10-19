package com.tumeet.backend.controller;

import com.tumeet.backend.dto.ActionResponse;
import com.tumeet.backend.dto.UpdateUsernameRequest;
import com.tumeet.backend.model.User;
import com.tumeet.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    @GetMapping("/{studentId}")
    public ResponseEntity<ActionResponse> getUserProfile(@PathVariable String studentId) {
        if (studentId == null || studentId.isBlank()) {
            return ResponseEntity.badRequest().body(new ActionResponse(false, "กรุณาระบุรหัสนักศึกษา"));
        }

        Optional<User> userOpt = userRepository.findByStudentId(studentId.trim());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new ActionResponse(false, "ไม่พบข้อมูลผู้ใช้สำหรับรหัส: " + studentId));
        }

        return ResponseEntity.ok(new ActionResponse(true, "ดึงข้อมูลผู้ใช้สำเร็จ", userOpt.get()));
    }
}
