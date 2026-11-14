package com.tumeet.backend.repository;

import com.tumeet.backend.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByEventIdOrderBySentAtAsc(Long eventId);

    Optional<ChatMessage> findTopByEventIdOrderBySentAtDesc(Long eventId);
}
