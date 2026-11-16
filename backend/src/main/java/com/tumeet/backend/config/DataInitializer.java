package com.tumeet.backend.config;

import com.tumeet.backend.model.Event;
import com.tumeet.backend.model.User;
import com.tumeet.backend.repository.EventRepository;
import com.tumeet.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedInitialData(UserRepository userRepository,
                                             EventRepository eventRepository,
                                             JdbcTemplate jdbcTemplate) {
        return args -> {
            // 0. Ensure event_participants.joined_at allows NULL and has default SYSDATETIME()
            try {
                jdbcTemplate.execute("""
                        IF OBJECT_ID('event_participants', 'U') IS NOT NULL
                        BEGIN
                            ALTER TABLE event_participants ALTER COLUMN joined_at datetime2 NULL;
                            IF NOT EXISTS (
                                SELECT 1 FROM sys.default_constraints
                                WHERE parent_object_id = OBJECT_ID('event_participants')
                                  AND parent_column_id = COLUMNPROPERTY(OBJECT_ID('event_participants'), 'joined_at', 'ColumnId')
                            )
                            BEGIN
                                ALTER TABLE event_participants
                                ADD CONSTRAINT DF_event_participants_joined_at DEFAULT SYSDATETIME() FOR joined_at;
                            END
                        END
                        """);
            } catch (Exception e) {
                // Ignore if running on in-memory test DB or non-MSSQL dialect
            }

            // 1. Seed 10 mock users (6800000000 - 6800000009)
            for (int i = 0; i <= 9; i++) {
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
                            "10",
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

            // Remove legacy mock user 6800000010 if exists
            userRepository.findByStudentId("6800000010").ifPresent(userRepository::delete);

            User defaultCreator = userRepository.findByStudentId("6800000000")
                    .or(() -> userRepository.findByStudentId("6800000001"))
                    .orElse(null);

            // 2. Seed initial mock events (4 active + 3 past history events) if no events exist yet
            if (eventRepository.count() == 0 && defaultCreator != null) {
                Event e1 = new Event("ตี๋น้อย", "food", "Mingle, ม.กรุงเทพ", 6, LocalDate.of(2026, 10, 25), "19.00", defaultCreator);
                e1.setCurrentParticipants(0);

                Event e2 = new Event("อ่านหนังสือ", "study", "ห้องสมุด", 3, LocalDate.of(2026, 10, 26), "18.00", defaultCreator);
                e2.setCurrentParticipants(1);

                Event e3 = new Event("ตีแบดมินตัน", "sport", "ยิม มธ.", 4, LocalDate.of(2026, 10, 27), "20.00", defaultCreator);
                e3.setCurrentParticipants(2);

                Event e4 = new Event("บอร์ดเกม", "bordgame", "Mingle, ม.กรุงเทพ", 6, LocalDate.of(2026, 10, 28), "13.00", defaultCreator);
                e4.setCurrentParticipants(4);

                Event past1 = new Event("อ่านหนังสือ", "study", "ห้องสมุดป๋วย", 4, LocalDate.of(2026, 10, 7), "19.00", defaultCreator);
                past1.setCurrentParticipants(4);

                Event past2 = new Event("ตี๋น้อย", "food", "Mingle, ม.กรุงเทพ", 6, LocalDate.of(2026, 10, 8), "19.00", defaultCreator);
                past2.setCurrentParticipants(6);

                Event past3 = new Event("ตี๋น้อย", "food", "Mingle, ม.กรุงเทพ", 6, LocalDate.of(2026, 10, 9), "19.00", defaultCreator);
                past3.setCurrentParticipants(6);

                eventRepository.saveAll(List.of(e1, e2, e3, e4, past1, past2, past3));
            } else if (defaultCreator != null) {
                // Ensure the 4 primary mock events (id 1..4) have active future dates so they can be joined
                eventRepository.findById(1L).ifPresent(ev -> {
                    if (ev.getEventDate().isBefore(LocalDate.of(2026, 10, 24))) {
                        ev.setEventDate(LocalDate.of(2026, 10, 25));
                        ev.setStatus("OPEN");
                        eventRepository.save(ev);
                    }
                });
                eventRepository.findById(2L).ifPresent(ev -> {
                    if (ev.getEventDate().isBefore(LocalDate.of(2026, 10, 24))) {
                        ev.setEventDate(LocalDate.of(2026, 10, 26));
                        ev.setStatus("OPEN");
                        eventRepository.save(ev);
                    }
                });
                eventRepository.findById(3L).ifPresent(ev -> {
                    if (ev.getEventDate().isBefore(LocalDate.of(2026, 10, 24))) {
                        ev.setEventDate(LocalDate.of(2026, 10, 27));
                        ev.setStatus("OPEN");
                        eventRepository.save(ev);
                    }
                });
                eventRepository.findById(4L).ifPresent(ev -> {
                    if (ev.getEventDate().isBefore(LocalDate.of(2026, 10, 24))) {
                        ev.setEventDate(LocalDate.of(2026, 10, 28));
                        ev.setStatus("OPEN");
                        eventRepository.save(ev);
                    }
                });

                // Seed the 3 past mock events into DB if not yet present
                List<Event> existing = eventRepository.findAllByOrderByIdAsc();
                boolean hasPastOct7 = existing.stream()
                        .anyMatch(ev -> LocalDate.of(2026, 10, 7).equals(ev.getEventDate()));
                if (!hasPastOct7) {
                    Event past1 = new Event("อ่านหนังสือ", "study", "ห้องสมุดป๋วย", 4, LocalDate.of(2026, 10, 7), "19.00", defaultCreator);
                    past1.setCurrentParticipants(4);
                    past1.setStatus("CLOSED");

                    Event past2 = new Event("ตี๋น้อย", "food", "Mingle, ม.กรุงเทพ", 6, LocalDate.of(2026, 10, 8), "19.00", defaultCreator);
                    past2.setCurrentParticipants(6);
                    past2.setStatus("CLOSED");

                    Event past3 = new Event("ตี๋น้อย", "food", "Mingle, ม.กรุงเทพ", 6, LocalDate.of(2026, 10, 9), "19.00", defaultCreator);
                    past3.setCurrentParticipants(6);
                    past3.setStatus("CLOSED");

                    eventRepository.saveAll(List.of(past1, past2, past3));
                }
            }
        };
    }
}
