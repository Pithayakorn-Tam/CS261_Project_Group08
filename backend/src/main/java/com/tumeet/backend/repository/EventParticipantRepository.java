package com.tumeet.backend.repository;

import com.tumeet.backend.model.EventParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventParticipantRepository extends JpaRepository<EventParticipant, Long> {

    boolean existsByEventIdAndUserStudentId(Long eventId, String studentId);

    Optional<EventParticipant> findByEventIdAndUserStudentId(Long eventId, String studentId);

    List<EventParticipant> findByEventId(Long eventId);

    List<EventParticipant> findByUserStudentId(String studentId);
}
