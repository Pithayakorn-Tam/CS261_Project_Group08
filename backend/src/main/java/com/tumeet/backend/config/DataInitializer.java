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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Configuration
public class DataInitializer {

    private record SeedEventSpec(
            String eventName,
            String eventType,
            String location,
            int eventSize,
            LocalDate eventDate,
            String eventTime,
            String status,
            String lifecycleStatus,
            int creatorIndex,
            int[] participantIndices
    ) {}

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
            User[] mockUsers = new User[10];
            for (int i = 0; i <= 9; i++) {
                String indexStr = String.format("%02d", i);
                String studentId = String.format("68000000%02d", i);

                mockUsers[i] = userRepository.findByStudentId(studentId).orElseGet(() -> {
                    String prefix = (Integer.parseInt(indexStr) % 2 != 0) ? "Mr" : "Ms";
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
                    return userRepository.save(mockUser);
                });
            }

            // Remove legacy mock user 6800000010 if exists
            userRepository.findByStudentId("6800000010").ifPresent(userRepository::delete);

            // 2. Define 7 consistent mock events (5 expired COMPLETED + 2 active UPCOMING near TU Rangsit)
            LocalDate today = LocalDate.now();
            List<SeedEventSpec> specs = List.of(
                    new SeedEventSpec("ตี๋น้อย", "food", "Mingle, ม.กรุงเทพ", 7, LocalDate.of(2026, 11, 3), "19.00", "CLOSED", "COMPLETED", 1, new int[]{0, 2, 3, 4, 5}),       // 6/7
                    new SeedEventSpec("อ่านหนังสือ", "study", "ห้องสมุด", 9, LocalDate.of(2026, 11, 4), "18.00", "CLOSED", "COMPLETED", 2, new int[]{0, 1, 3, 4, 5, 6, 7, 8}), // 9/9
                    new SeedEventSpec("ตีแบดมินตัน", "sport", "ยิม มธ.", 3, LocalDate.of(2026, 11, 5), "20.00", "CLOSED", "COMPLETED", 3, new int[]{0, 4}),                    // 3/3
                    new SeedEventSpec("บอร์ดเกม", "bordgame", "Mingle, ม.กรุงเทพ", 5, LocalDate.of(2026, 11, 6), "13.00", "CLOSED", "COMPLETED", 4, new int[]{0, 5, 6}),          // 4/5
                    new SeedEventSpec("อ่านหนังสือ", "study", "ห้องสมุดป๋วย", 3, LocalDate.of(2026, 10, 7), "19.00", "CLOSED", "COMPLETED", 0, new int[]{1}),                  // 2/3 (User00 created & completed)
                    new SeedEventSpec("หมูกระทะ", "food", "ตลาดนัดอินเตอร์โซน มธ.รังสิต", 6, today.plusDays(2), "19.00", "OPEN", "UPCOMING", 0, new int[]{2, 3}),                // 3/6 (User00 created & active)
                    new SeedEventSpec("เล่นบอร์ดเกม", "bordgame", "U-Square มธ.รังสิต", 5, today.plusDays(3), "20.00", "OPEN", "UPCOMING", 6, new int[]{7})                   // 2/5 (Active near TU)
            );

            // Clean legacy test/extra past events if upgrading an existing DB
            List<Event> existingEvents = eventRepository.findAllByOrderByIdAsc();
            for (Event ev : existingEvents) {
                if (LocalDate.of(2026, 10, 8).equals(ev.getEventDate())
                        || LocalDate.of(2026, 10, 9).equals(ev.getEventDate())
                        || "test".equalsIgnoreCase(ev.getEventName())) {
                    if (ev.getParticipants() != null) {
                        ev.getParticipants().clear();
                    }
                    eventRepository.delete(ev);
                }
            }

            if (eventRepository.count() == 0) {
                for (SeedEventSpec spec : specs) {
                    Event ev = new Event();
                    applySpecToEvent(ev, spec, mockUsers);
                    eventRepository.save(ev);
                }
            } else {
                // Reconcile the 5 core past events (by id 1..4 and 2026-10-07) and ensure events 6 & 7 exist
                List<Event> currentEvents = eventRepository.findAllByOrderByIdAsc();
                for (int i = 0; i < specs.size(); i++) {
                    SeedEventSpec spec = specs.get(i);
                    Optional<Event> matched = Optional.empty();
                    if (i < 4) {
                        long targetId = i + 1L;
                        matched = currentEvents.stream().filter(e -> targetId == e.getId()).findFirst();
                    } else if (i == 4) {
                        matched = currentEvents.stream()
                                .filter(e -> LocalDate.of(2026, 10, 7).equals(e.getEventDate()))
                                .findFirst();
                    } else {
                        matched = currentEvents.stream()
                                .filter(e -> spec.eventName().equals(e.getEventName()) && spec.eventTime().equals(e.getEventTime()))
                                .findFirst();
                    }
                    Event ev = matched.orElseGet(Event::new);
                    applySpecToEvent(ev, spec, mockUsers);
                    eventRepository.save(ev);
                }
            }

            // 3. Ensure any event without a creator gets a random mock creator, and sync participant counts
            Random random = new Random();
            List<Event> allEvents = eventRepository.findAllByOrderByIdAsc();
            for (Event ev : allEvents) {
                boolean changed = false;
                if (ev.getCreator() == null) {
                    ev.setCreator(mockUsers[random.nextInt(mockUsers.length)]);
                    changed = true;
                }
                int joinedCount = ev.getParticipants() != null ? ev.getParticipants().size() : 0;
                int expectedParticipants = 1 + joinedCount;
                if (ev.getCurrentParticipants() == null || ev.getCurrentParticipants() < expectedParticipants) {
                    ev.setCurrentParticipants(expectedParticipants);
                    changed = true;
                }
                if (changed) {
                    eventRepository.save(ev);
                }
            }

            // 4. Recalculate completed_created_count and completed_joined_count for all users from COMPLETED events
            List<User> allUsers = userRepository.findAll();
            Map<Long, Integer> createdCounts = new HashMap<>();
            Map<Long, Integer> joinedCounts = new HashMap<>();
            for (User u : allUsers) {
                createdCounts.put(u.getId(), 0);
                joinedCounts.put(u.getId(), 0);
            }

            for (Event ev : allEvents) {
                if (!"COMPLETED".equalsIgnoreCase(ev.getLifecycleStatus())) {
                    continue;
                }
                int memberCount = (ev.getParticipants() != null ? ev.getParticipants().size() : 0) + 1;
                if (memberCount < 2 && (ev.getCurrentParticipants() == null || ev.getCurrentParticipants() < 2)) {
                    continue;
                }
                if (ev.getCreator() != null) {
                    createdCounts.merge(ev.getCreator().getId(), 1, Integer::sum);
                }
                if (ev.getParticipants() != null) {
                    for (User p : ev.getParticipants()) {
                        joinedCounts.merge(p.getId(), 1, Integer::sum);
                    }
                }
            }

            for (User u : allUsers) {
                u.setCompletedCreatedCount(createdCounts.getOrDefault(u.getId(), 0));
                u.setCompletedJoinedCount(joinedCounts.getOrDefault(u.getId(), 0));
            }
            userRepository.saveAll(allUsers);
        });
    }

    private void applySpecToEvent(Event ev, SeedEventSpec spec, User[] mockUsers) {
        ev.setEventName(spec.eventName());
        ev.setEventType(spec.eventType());
        ev.setLocation(spec.location());
        ev.setEventSize(spec.eventSize());
        ev.setEventDate(spec.eventDate());
        ev.setEventTime(spec.eventTime());
        ev.setStatus(spec.status());
        ev.setLifecycleStatus(spec.lifecycleStatus());
        User creator = mockUsers[spec.creatorIndex()];
        ev.setCreator(creator);

        Set<User> participants = new LinkedHashSet<>();
        for (int pIdx : spec.participantIndices()) {
            User p = mockUsers[pIdx];
            if (p != null && !p.getStudentId().equals(creator.getStudentId())) {
                participants.add(p);
            }
        }
        ev.setParticipants(participants);
        ev.setCurrentParticipants(1 + participants.size());
    }
}
