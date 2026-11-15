package com.tumeet.backend.service;

import com.tumeet.backend.dto.ActionResponse;
import com.tumeet.backend.dto.NotificationDto;
import com.tumeet.backend.model.Event;
import com.tumeet.backend.model.Notification;
import com.tumeet.backend.model.User;
import com.tumeet.backend.repository.EventRepository;
import com.tumeet.backend.repository.NotificationRepository;
import com.tumeet.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class NotificationService {

    public static final String TYPE_CREATE_SUCCESS = "CREATE_SUCCESS";
    public static final String TYPE_CANCEL_SUCCESS = "CANCEL_SUCCESS";
    public static final String TYPE_JOIN_SUCCESS = "JOIN_SUCCESS";
    public static final String TYPE_USER_JOINED = "USER_JOINED";
    public static final String TYPE_LEAVE_SUCCESS = "LEAVE_SUCCESS";
    public static final String TYPE_USER_LEFT = "USER_LEFT";
    public static final String TYPE_EVENT_CANCELLED = "EVENT_CANCELLED";
    public static final String TYPE_EVENT_REMINDER = "EVENT_REMINDER";
    public static final String TYPE_EVENT_COMPLETED = "EVENT_COMPLETED";
    public static final String TYPE_BADGE_UPGRADED = "BADGE_UPGRADED";

    private final NotificationRepository notificationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository,
                               EventRepository eventRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void notifyEventCreated(Event event) {
        if (event == null || event.getCreator() == null) {
            return;
        }
        saveIndividualNotification(
                event.getCreator(),
                event,
                TYPE_CREATE_SUCCESS,
                "สร้างกิจกรรมสำเร็จ",
                "คุณสร้างกิจกรรมสำเร็จ"
        );
    }

    @Transactional
    public void notifyUserJoined(Event event, User participant) {
        if (event == null || participant == null) {
            return;
        }
        saveIndividualNotification(
                participant,
                event,
                TYPE_JOIN_SUCCESS,
                "เข้าร่วมกิจกรรมสำเร็จ",
                "คุณเข้าร่วมกิจกรรมสำเร็จ"
        );

        User creator = event.getCreator();
        if (creator != null && !isSameUser(creator, participant)) {
            saveIndividualNotification(
                    creator,
                    event,
                    TYPE_USER_JOINED,
                    "มีผู้เข้าร่วมกิจกรรม",
                    "มีผู้เข้าร่วมกิจกรรมของคุณ"
            );
        }
    }

    @Transactional
    public void notifyUserLeft(Event event, User participant) {
        if (event == null || participant == null) {
            return;
        }
        saveIndividualNotification(
                participant,
                event,
                TYPE_LEAVE_SUCCESS,
                "ยกเลิกการเข้าร่วมกิจกรรม",
                "คุณได้ยกเลิกการเข้าร่วมกิจกรรม"
        );

        User creator = event.getCreator();
        if (creator != null && !isSameUser(creator, participant)) {
            saveIndividualNotification(
                    creator,
                    event,
                    TYPE_USER_LEFT,
                    "มีผู้ยกเลิกการเข้าร่วมกิจกรรม",
                    "มีผู้ยกเลิกการเข้าร่วมกิจกรรมของคุณ"
            );
        }
    }

    @Transactional
    public void notifyEventCancelled(Event event) {
        if (event == null) {
            return;
        }
        User creator = event.getCreator();
        if (creator != null) {
            saveIndividualNotification(
                    creator,
                    event,
                    TYPE_CANCEL_SUCCESS,
                    "ยกเลิกการสร้างกิจกรรม",
                    "คุณได้ยกเลิกการสร้างกิจกรรม"
            );
        }
        if (event.getParticipants() == null) {
            return;
        }
        String eventName = safeEventName(event);
        String msg = "กิจกรรม “" + eventName + "” ที่คุณเข้าร่วมถูกยกเลิกโดยผู้จัดกิจกรรม";

        for (User participant : event.getParticipants()) {
            if (participant != null && !isSameUser(creator, participant)) {
                saveIndividualNotification(
                        participant,
                        event,
                        TYPE_EVENT_CANCELLED,
                        "กิจกรรมถูกยกเลิก",
                        msg
                );
            }
        }
    }

    @Transactional
    public void notifyEventCompleted(Event event, User recipient) {
        if (event == null || recipient == null) {
            return;
        }
        if (recipient.getId() != null && event.getId() != null
                && notificationRepository.existsByRecipientIdAndEventIdAndType(
                        recipient.getId(), event.getId(), TYPE_EVENT_COMPLETED)) {
            return;
        }
        String eventName = safeEventName(event);
        saveIndividualNotification(
                recipient,
                event,
                TYPE_EVENT_COMPLETED,
                "กิจกรรมเสร็จสิ้น",
                "กิจกรรม “" + eventName + "” สิ้นสุดแล้ว! คุณได้รับแต้มกิจกรรมสำเร็จ +1 และสามารถประเมินคะแนนเพื่อนร่วมกิจกรรมได้แล้ว"
        );
    }

    @Transactional
    public void notifyBadgeUpgradedIfEligible(User user, Event event, int previousTotal, int newTotal) {
        if (user == null || newTotal <= previousTotal) {
            return;
        }
        String badgeTitle = null;
        String badgeMessage = null;

        if (newTotal >= 20 && previousTotal < 20) {
            badgeTitle = "Super Bouquet";
            badgeMessage = "ยศ Super Bouquet ขั้นสุดของนักกิจกรรม";
        } else if (newTotal >= 10 && previousTotal < 10) {
            badgeTitle = "Happy Blossom";
            badgeMessage = "ถึงขั้น Happy Blossom แล้ว ! สนุกกับการทำกิจกรรมต่อไปนะ";
        } else if (newTotal >= 5 && previousTotal < 5) {
            badgeTitle = "Baby Bloom";
            badgeMessage = "เลื่อนขั้นเป็น Baby Bloom นักท่องโลกกำลังเติบโต";
        } else if (newTotal >= 1 && previousTotal < 1) {
            badgeTitle = "Newly Bee";
            badgeMessage = "เข้าร่วมกิจกรรมครั้งแรก !\nคุณได้รับยศ Newly Bee";
        }

        if (badgeTitle == null) {
            return;
        }

        if (user.getId() != null
                && notificationRepository.existsByRecipientIdAndTypeAndTitle(user.getId(), TYPE_BADGE_UPGRADED, badgeTitle)) {
            return;
        }

        saveIndividualNotification(user, event, TYPE_BADGE_UPGRADED, badgeTitle, badgeMessage);
    }

    @Transactional
    public void syncUpcomingEventReminders() {
        List<Event> upcomingEvents = eventRepository.findByLifecycleStatus("UPCOMING");
        if (upcomingEvents == null || upcomingEvents.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (Event event : upcomingEvents) {
            LocalDateTime eventStart = parseExactEventDateTime(event.getEventDate(), event.getEventTime());
            if (eventStart == null) {
                continue;
            }
            if (now.isBefore(eventStart) && !now.isBefore(eventStart.minusMinutes(30))) {
                Set<User> recipients = collectEventMembers(event);
                String msg = "อีก 30 นาที “" + safeEventName(event) + "” จะเริ่มขึ้น";
                for (User recipient : recipients) {
                    if (recipient.getId() != null && event.getId() != null
                            && notificationRepository.existsByRecipientIdAndEventIdAndType(
                                    recipient.getId(), event.getId(), TYPE_EVENT_REMINDER)) {
                        continue;
                    }
                    saveIndividualNotification(recipient, event, TYPE_EVENT_REMINDER, "กิจกรรมใกล้เริ่ม", msg);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public ActionResponse getNotificationsForStudent(String studentId) {
        if (studentId == null || studentId.isBlank()) {
            return new ActionResponse(false, "กรุณาระบุรหัสนักศึกษา");
        }
        String trimmedId = studentId.trim();
        Optional<User> userOpt = userRepository.findByStudentId(trimmedId);
        if (userOpt.isEmpty()) {
            return new ActionResponse(false, "ไม่พบข้อมูลผู้ใช้สำหรับรหัสนักศึกษา: " + trimmedId);
        }

        List<NotificationDto> items = notificationRepository
                .findByRecipientStudentIdOrderByCreatedAtDescIdDesc(trimmedId)
                .stream()
                .map(NotificationDto::fromEntity)
                .toList();

        long unreadCount = notificationRepository.countByRecipientStudentIdAndIsReadFalse(trimmedId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("notifications", items);
        payload.put("unreadCount", unreadCount);

        return new ActionResponse(true, "ดึงรายการแจ้งเตือนสำเร็จ", payload);
    }

    @Transactional
    public ActionResponse markAsRead(Long notificationId) {
        if (notificationId == null) {
            return new ActionResponse(false, "กรุณาระบุรหัสแจ้งเตือน");
        }
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            return new ActionResponse(false, "ไม่พบแจ้งเตือนรหัส: " + notificationId);
        }

        Notification notification = notificationOpt.get();
        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification = notificationRepository.save(notification);
        }

        return new ActionResponse(true, "อัปเดตสถานะอ่านแจ้งเตือนสำเร็จ", NotificationDto.fromEntity(notification));
    }

    @Transactional
    public ActionResponse markAllAsRead(String studentId) {
        if (studentId == null || studentId.isBlank()) {
            return new ActionResponse(false, "กรุณาระบุรหัสนักศึกษา");
        }
        String trimmedId = studentId.trim();
        Optional<User> userOpt = userRepository.findByStudentId(trimmedId);
        if (userOpt.isEmpty()) {
            return new ActionResponse(false, "ไม่พบข้อมูลผู้ใช้สำหรับรหัสนักศึกษา: " + trimmedId);
        }

        List<Notification> unread = notificationRepository.findByRecipientStudentIdAndIsReadFalse(trimmedId);
        for (Notification n : unread) {
            n.setIsRead(true);
        }
        if (!unread.isEmpty()) {
            notificationRepository.saveAll(unread);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("updatedCount", unread.size());
        payload.put("unreadCount", 0L);

        return new ActionResponse(true, "อ่านแจ้งเตือนทั้งหมดแล้ว", payload);
    }

    private Notification saveIndividualNotification(User recipient, Event event, String type, String title, String message) {
        Notification notification = new Notification(recipient, event, type, title, message);
        return notificationRepository.save(notification);
    }

    private Set<User> collectEventMembers(Event event) {
        Set<User> members = new LinkedHashSet<>();
        if (event.getCreator() != null) {
            members.add(event.getCreator());
        }
        if (event.getParticipants() != null) {
            for (User participant : event.getParticipants()) {
                if (participant != null) {
                    members.add(participant);
                }
            }
        }
        return members;
    }

    private LocalDateTime parseExactEventDateTime(LocalDate eventDate, String eventTime) {
        if (eventDate == null || eventTime == null || eventTime.isBlank()) {
            return null;
        }
        try {
            String normalizedTime = eventTime.trim().replace('.', ':');
            String[] parts = normalizedTime.split(":");
            if (parts.length == 2 && parts[0].length() == 1) {
                normalizedTime = "0" + normalizedTime;
            }
            LocalTime parsedTime = LocalTime.parse(normalizedTime);
            return LocalDateTime.of(eventDate, parsedTime);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private boolean isSameUser(User first, User second) {
        if (first == null || second == null) {
            return false;
        }
        if (first.getId() != null && second.getId() != null) {
            return first.getId().equals(second.getId());
        }
        if (first.getStudentId() != null && second.getStudentId() != null) {
            return first.getStudentId().equals(second.getStudentId());
        }
        return false;
    }

    private String safeEventName(Event event) {
        return event.getEventName() != null && !event.getEventName().isBlank()
                ? event.getEventName().trim()
                : "กิจกรรม";
    }
}
