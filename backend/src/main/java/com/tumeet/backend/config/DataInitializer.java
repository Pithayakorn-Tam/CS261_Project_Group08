package com.tumeet.backend.config;

import com.tumeet.backend.model.Event;
import com.tumeet.backend.model.User;
import com.tumeet.backend.repository.EventRepository;
import com.tumeet.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedInitialData(UserRepository userRepository, EventRepository eventRepository) {
        return args -> {
            // 1. Seed 10 mock users (6800000001 - 6800000010)
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

            // 2. Seed 4 default events if no events exist yet
            if (eventRepository.count() == 0) {
                User defaultCreator = userRepository.findByStudentId("6800000001").orElse(null);
                if (defaultCreator != null) {
                    Event e1 = new Event("ตี๋น้อย", "food", "Mingle, ม.กรุงเทพ", 6, LocalDate.of(2026, 10, 10), "19.00", defaultCreator);
                    e1.setCurrentParticipants(0);

                    Event e2 = new Event("อ่านหนังสือ", "study", "ห้องสมุด", 3, LocalDate.of(2026, 10, 12), "18.00", defaultCreator);
                    e2.setCurrentParticipants(1);

                    Event e3 = new Event("ตีแบดมินตัน", "sport", "ยิม มธ.", 4, LocalDate.of(2026, 10, 13), "20.00", defaultCreator);
                    e3.setCurrentParticipants(2);

                    Event e4 = new Event("บอร์ดเกม", "bordgame", "Mingle, ม.กรุงเทพ", 6, LocalDate.of(2026, 10, 18), "13.00", defaultCreator);
                    e4.setCurrentParticipants(4);

                    eventRepository.saveAll(List.of(e1, e2, e3, e4));
                }
            }
        };
    }
}
