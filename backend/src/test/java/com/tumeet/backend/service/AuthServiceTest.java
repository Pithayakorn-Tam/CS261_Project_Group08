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
        mockUser.setStudentId("6800000000");
        mockUser.setEmail("mock00@dome.tu.ac.th");
        mockUser.setDisplayname_th("นักศึกษาจำลอง ทดสอบ00");
    }

    @Test
    @DisplayName("เข้าสู่ระบบข้อมูลจำลอง 6800000000 สำเร็จเมื่อรหัสผ่านคือ 1111111111111")
    void authenticate_MockUser_Success() {
        when(userRepository.findByStudentId("6800000000")).thenReturn(Optional.of(mockUser));

        LoginRequest request = new LoginRequest("6800000000", "1111111111111");
        LoginResponse response = authService.authenticate(request);

        assertTrue(response.isStatus());
        assertEquals("Login Success", response.getMessage());
        assertEquals("6800000000", response.getStudentId());
        assertEquals("mock00@dome.tu.ac.th", response.getEmail());
        assertEquals("นักศึกษาจำลอง ทดสอบ00", response.getDisplayname_th());

        // ตรวจสอบว่าค้นหาแค่ใน DB ไม่มีการเรียกต่อ
        verify(userRepository, times(1)).findByStudentId("6800000000");
    }

    @Test
    @DisplayName("เข้าสู่ระบบข้อมูลจำลองล้มเหลวทันทีเมื่อรหัสผ่านไม่ใช่ 1111111111111")
    void authenticate_MockUser_WrongPassword_Fails() {
        LoginRequest request = new LoginRequest("6800000000", "wrong_password");
        LoginResponse response = authService.authenticate(request);

        assertFalse(response.isStatus());
        assertEquals("รหัสนักศึกษาหรือรหัสผ่านไม่ถูกต้อง", response.getMessage());

        // ไม่มีการค้นหา DB หรือเรียก API ภายนอก
        verify(userRepository, never()).findByStudentId(anyString());
    }

    @Test
    @DisplayName("เข้าสู่ระบบข้อมูลจำลอง 6800000009 สำเร็จเมื่อรหัสผ่านคือ 1111111111111")
    void authenticate_MockUser_Boundary09_Success() {
        User mock09 = new User();
        mock09.setStudentId("6800000009");
        mock09.setEmail("mock09@dome.tu.ac.th");
        when(userRepository.findByStudentId("6800000009")).thenReturn(Optional.of(mock09));

        LoginRequest request = new LoginRequest("6800000009", "1111111111111");
        LoginResponse response = authService.authenticate(request);

        assertTrue(response.isStatus());
        assertEquals("6800000009", response.getStudentId());
    }
}
