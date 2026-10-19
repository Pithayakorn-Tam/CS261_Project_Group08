package com.tumeet.backend.service;

import com.tumeet.backend.dto.LoginRequest;
import com.tumeet.backend.dto.LoginResponse;
import com.tumeet.backend.model.User;
import com.tumeet.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setStudentId("6800000001");
        mockUser.setEmail("mock01@dome.tu.ac.th");
    }

    @Test
    @DisplayName("เข้าสู่ระบบสำเร็จเมื่อข้อมูลรหัสนักศึกษาและอีเมลตรงกันในฐานข้อมูล")
    void authenticate_Success() {
        LoginRequest request = new LoginRequest("mock01@dome.tu.ac.th", "6800000001");
        when(userRepository.findByStudentId("6800000001")).thenReturn(Optional.of(mockUser));

        LoginResponse response = authService.authenticate(request);

        assertTrue(response.isStatus());
        assertEquals("Login Success", response.getMessage());
    }

    @Test
    @DisplayName("เข้าสู่ระบบล้มเหลวเมื่ออีเมลไม่ตรงกับรหัสนักศึกษาในฐานข้อมูล")
    void authenticate_EmailMismatch() {
        LoginRequest request = new LoginRequest("wrong@dome.tu.ac.th", "6800000001");
        when(userRepository.findByStudentId("6800000001")).thenReturn(Optional.of(mockUser));

        LoginResponse response = authService.authenticate(request);

        assertFalse(response.isStatus());
        assertTrue(response.getMessage().contains("Email does not match"));
    }

    @Test
    @DisplayName("เข้าสู่ระบบล้มเหลวเมื่อไม่ระบุรหัสนักศึกษาหรืออีเมล")
    void authenticate_MissingFields() {
        LoginRequest request = new LoginRequest("", "");

        LoginResponse response = authService.authenticate(request);

        assertFalse(response.isStatus());
        assertTrue(response.getMessage().contains("required"));
    }

    @Test
    @DisplayName("เข้าสู่ระบบล้มเหลวเมื่อไม่พบผู้ใช้ในระบบและยังไม่ได้ตั้งค่า TU API Key")
    void authenticate_UserNotFound_NoApiKey() {
        LoginRequest request = new LoginRequest("newuser@dome.tu.ac.th", "9999999999");
        when(userRepository.findByStudentId("9999999999")).thenReturn(Optional.empty());

        LoginResponse response = authService.authenticate(request);

        assertFalse(response.isStatus());
        assertTrue(response.getMessage().contains("not found"));
    }
}
