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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerProfileImageTest {

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
    @DisplayName("PUT /api/user/update-profile-image คืนค่า 200 OK และอัปเดตรูปโปรไฟล์สำเร็จ")
    void updateProfileImage_Success() throws Exception {
        String requestJson = """
                {
                    "studentId": "6800000001",
                    "profileImage": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUg=="
                }
                """;

        when(userRepository.findByStudentId("6800000001")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        mockMvc.perform(put("/api/user/update-profile-image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("อัปเดตรูปโปรไฟล์สำเร็จ"));

        verify(userRepository).save(mockUser);
    }

    @Test
    @DisplayName("PUT /api/user/update-profile-image คืนค่า 400 เมื่อไม่ระบุ studentId")
    void updateProfileImage_MissingStudentId() throws Exception {
        String requestJson = """
                {
                    "studentId": "",
                    "profileImage": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUg=="
                }
                """;

        mockMvc.perform(put("/api/user/update-profile-image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("กรุณาระบุรหัสนักศึกษา"));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("PUT /api/user/update-profile-image คืนค่า 400 เมื่อไม่ระบุ profileImage")
    void updateProfileImage_MissingImage() throws Exception {
        String requestJson = """
                {
                    "studentId": "6800000001",
                    "profileImage": ""
                }
                """;

        mockMvc.perform(put("/api/user/update-profile-image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("กรุณาระบุรูปโปรไฟล์"));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("PUT /api/user/update-profile-image คืนค่า 400 เมื่อ profileImage ไม่ใช่ Data URL")
    void updateProfileImage_InvalidFormat() throws Exception {
        String requestJson = """
                {
                    "studentId": "6800000001",
                    "profileImage": "https://example.com/image.png"
                }
                """;

        mockMvc.perform(put("/api/user/update-profile-image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("รูปโปรไฟล์ต้องเป็น Base64 Data URL ที่ขึ้นต้นด้วย data:image/"));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("PUT /api/user/update-profile-image คืนค่า 400 เมื่อไม่พบผู้ใช้")
    void updateProfileImage_UserNotFound() throws Exception {
        String requestJson = """
                {
                    "studentId": "9999999999",
                    "profileImage": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUg=="
                }
                """;

        when(userRepository.findByStudentId("9999999999")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/user/update-profile-image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("ไม่พบข้อมูลผู้ใช้สำหรับรหัส: 9999999999"));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("GET /api/user/{studentId} ส่ง profileImage ติดกลับมาด้วย")
    void getUserProfile_IncludesProfileImage() throws Exception {
        mockUser.setProfileImage("data:image/png;base64,iVBORw0KGgoAAAANSUhEUg==");
        when(userRepository.findByStudentId("6800000001")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/user/6800000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.data.profileImage").value("data:image/png;base64,iVBORw0KGgoAAAANSUhEUg=="));
    }
}
