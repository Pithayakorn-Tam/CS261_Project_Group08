package com.tumeet.backend.controller;

import com.tumeet.backend.model.User;
import com.tumeet.backend.repository.UserRepository;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController userController;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setStudentId("6800000001");
        mockUser.setEmail("mock01@dome.tu.ac.th");
        mockUser.setDisplayname_th("นักศึกษาทดสอบ");
    }

    @Test
    @DisplayName("PUT /api/user/update-username คืนค่า 200 OK และอัปเดตชื่อสำเร็จ")
    void updateUsername_Success() throws Exception {
        String requestJson = """
                {
                    "studentId": "6800000001",
                    "username": "มะหมาสี่ขา"
                }
                """;

        when(userRepository.findByStudentId("6800000001")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(put("/api/user/update-username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("อัปเดตชื่อผู้ใช้สำเร็จ"))
                .andExpect(jsonPath("$.data.username").value("มะหมาสี่ขา"));

        verify(userRepository).save(mockUser);
    }

    @Test
    @DisplayName("GET /api/user/{studentId} คืนค่า 200 OK พร้อมข้อมูลผู้ใช้")
    void getUserProfile_Success() throws Exception {
        when(userRepository.findByStudentId("6800000001")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/user/6800000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.data.studentId").value("6800000001"));
    }
}
