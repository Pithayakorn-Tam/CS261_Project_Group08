package com.tumeet.backend.config;

import com.tumeet.backend.model.Event;
import com.tumeet.backend.model.User;
import com.tumeet.backend.repository.EventRepository;
import com.tumeet.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedInitialData(UserRepository userRepository,
                                             EventRepository eventRepository,
                                             JdbcTemplate jdbcTemplate,
                                             TransactionTemplate transactionTemplate) {
        return args -> transactionTemplate.executeWithoutResult(status -> {
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

            User defaultCreator = userRepository.findByStudentId("6800000009")
                    .or(() -> userRepository.findByStudentId("6800000000"))
                    .orElse(null);

            // 2. Seed initial 5 mock events (4 active in Nov 3-6 + 1 past expired event) if no events exist yet
            if (eventRepository.count() == 0 && defaultCreator != null) {
                Event e1 = new Event("ตี๋น้อย", "food", "Mingle, ม.กรุงเทพ", 6, LocalDate.of(2026, 11, 3), "19.00", defaultCreator);
                e1.setCurrentParticipants(1);

                Event e2 = new Event("อ่านหนังสือ", "study", "ห้องสมุด", 3, LocalDate.of(2026, 11, 4), "18.00", defaultCreator);
                e2.setCurrentParticipants(1);

                Event e3 = new Event("ตีแบดมินตัน", "sport", "ยิม มธ.", 4, LocalDate.of(2026, 11, 5), "20.00", defaultCreator);
                e3.setCurrentParticipants(1);

                Event e4 = new Event("บอร์ดเกม", "bordgame", "Mingle, ม.กรุงเทพ", 6, LocalDate.of(2026, 11, 6), "13.00", defaultCreator);
                e4.setCurrentParticipants(1);

                Event past1 = new Event("อ่านหนังสือ", "study", "ห้องสมุดป๋วย", 4, LocalDate.of(2026, 10, 7), "19.00", defaultCreator);
                past1.setCurrentParticipants(4);
                past1.setStatus("CLOSED");

                eventRepository.saveAll(List.of(e1, e2, e3, e4, past1));
            } else if (defaultCreator != null) {
                // Ensure the 4 primary mock events (id 1..4) have creator 6800000009, Nov 3-6 dates, and start at 1 (+ any joined participants)
                LocalDate[] targetDates = {
                        LocalDate.of(2026, 11, 3),
                        LocalDate.of(2026, 11, 4),
                        LocalDate.of(2026, 11, 5),
                        LocalDate.of(2026, 11, 6)
                };
                for (int idx = 0; idx < 4; idx++) {
                    long eventId = idx + 1L;
                    LocalDate targetDate = targetDates[idx];
                    eventRepository.findById(eventId).ifPresent(ev -> {
                        ev.setCreator(defaultCreator);
                        ev.setEventDate(targetDate);
                        int joinedCount = ev.getParticipants() != null ? ev.getParticipants().size() : 0;
                        int expectedCount = 1 + joinedCount;
                        ev.setCurrentParticipants(expectedCount);
                        ev.setStatus(expectedCount >= ev.getEventSize() ? "FULL" : "OPEN");
                        eventRepository.save(ev);
                    });
                }

                // Keep only 1 expired seeded event (2026-10-07) and remove extra seeded past events (2026-10-08, 2026-10-09)
                List<Event> existing = eventRepository.findAllByOrderByIdAsc();
                boolean hasPastOct7 = false;
                for (Event ev : existing) {
                    if (LocalDate.of(2026, 10, 8).equals(ev.getEventDate())
                            || LocalDate.of(2026, 10, 9).equals(ev.getEventDate())
                            || "test".equalsIgnoreCase(ev.getEventName())) {
                        if (ev.getParticipants() != null) {
                            ev.getParticipants().clear();
                        }
                        eventRepository.delete(ev);
                        continue;
                    }
                    if (LocalDate.of(2026, 10, 7).equals(ev.getEventDate())) {
                        hasPastOct7 = true;
                        ev.setCreator(defaultCreator);
                        ev.setCurrentParticipants(4);
                        ev.setStatus("CLOSED");
                        eventRepository.save(ev);
                        continue;
                    }
                    int joinedCount = ev.getParticipants() != null ? ev.getParticipants().size() : 0;
                    int minExpected = 1 + joinedCount;
                    if (ev.getCurrentParticipants() == null || ev.getCurrentParticipants() < minExpected) {
                        ev.setCurrentParticipants(minExpected);
                        eventRepository.save(ev);
                    }
                }

                if (!hasPastOct7) {
                    Event past1 = new Event("อ่านหนังสือ", "study", "ห้องสมุดป๋วย", 4, LocalDate.of(2026, 10, 7), "19.00", defaultCreator);
                    past1.setCurrentParticipants(4);
                    past1.setStatus("CLOSED");
                    eventRepository.save(past1);
                }
            }
        });
    }
}
