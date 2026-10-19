package com.tumeet.backend.controller;

import com.tumeet.backend.dto.LoginRequest;
import com.tumeet.backend.dto.LoginResponse;
import com.tumeet.backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    @DisplayName("POST /api/login คืนค่า 200 OK เมื่อข้อมูลเข้าสู่ระบบถูกต้อง")
    void login_Success_ReturnsOk() throws Exception {
        String requestJson = """
                {
                    "studentId": "6800000001",
                    "email": "mock01@dome.tu.ac.th"
                }
                """;

        when(authService.authenticate(any(LoginRequest.class)))
                .thenReturn(LoginResponse.success());

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Login Success"));
    }

    @Test
    @DisplayName("POST /api/login คืนค่า 401 Unauthorized เมื่อข้อมูลไม่ถูกต้อง")
    void login_Failed_ReturnsUnauthorized() throws Exception {
        String requestJson = """
                {
                    "studentId": "6800000001",
                    "email": "wrong@dome.tu.ac.th"
                }
                """;

        when(authService.authenticate(any(LoginRequest.class)))
                .thenReturn(LoginResponse.error("Email does not match the student ID in database"));

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("Email does not match the student ID in database"));
    }
}
