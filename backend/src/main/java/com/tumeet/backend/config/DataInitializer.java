package com.tumeet.backend.config;

import com.tumeet.backend.model.User;
import com.tumeet.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedMockUsers(UserRepository userRepository) {
        return args -> {
            for (int i = 1; i <= 10; i++) {
                String indexStr = String.format("%02d", i);
                String studentId = String.format("68000000%02d", i);

                if (!userRepository.existsByStudentId(studentId)) {
                    String prefix = (i % 2 != 0) ? "Mr" : "Ms";
                    String email = "mock" + indexStr + "@dome.tu.ac.th";
                    String displayNameTh = "นักศึกษาจำลอง ทดสอบ" + indexStr;
                    String displayNameEn = "Mock Student " + indexStr;

                    User mockUser = new User(
                            studentId,
                            email,
                            "student",
                            "ปกติ",
                            prefix,
                            displayNameTh,
                            displayNameEn,
                            "คณะวิทยาศาสตร์และเทคโนโลยี",
                            "สาขาวิชาวิทยาการคอมพิวเตอร์"
                    );

                    userRepository.save(mockUser);
                }
            }
        };
    }
}
