package com.tumeet.backend.repository;

import com.tumeet.backend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientStudentIdOrderByCreatedAtDescIdDesc(String studentId);

    List<Notification> findByRecipientStudentIdAndIsReadFalse(String studentId);

    long countByRecipientStudentIdAndIsReadFalse(String studentId);

    boolean existsByRecipientIdAndEventIdAndType(Long recipientId, Long eventId, String type);

    boolean existsByRecipientIdAndTypeAndTitle(Long recipientId, String type, String title);
}
