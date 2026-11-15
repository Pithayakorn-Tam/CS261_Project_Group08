package com.tumeet.backend.service;

import com.tumeet.backend.dto.ActionResponse;
import com.tumeet.backend.model.Event;
import com.tumeet.backend.model.Notification;
import com.tumeet.backend.model.User;
import com.tumeet.backend.repository.EventRepository;
import com.tumeet.backend.repository.NotificationRepository;
import com.tumeet.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User creator;
    private User participant1;
    private User participant2;
    private Event event;

    @BeforeEach
    void setUp() {
        creator = new User();
        creator.setId(1L);
        creator.setStudentId("6800000000");

        participant1 = new User();
        participant1.setId(2L);
        participant1.setStudentId("6800000001");

        participant2 = new User();
        participant2.setId(3L);
        participant2.setStudentId("6800000002");

        event = new Event();
        event.setId(100L);
        event.setEventName("ตี๋น้อยที่ซอยห้า");
        event.setCreator(creator);
        event.setLifecycleStatus("UPCOMING");
        event.getParticipants().add(participant1);
        event.getParticipants().add(participant2);
    }

    @Test
    @DisplayName("notifyUserJoined สร้างแจ้งเตือนแยกให้ผู้เข้าร่วมและผู้สร้างตามข้อความมาตรฐาน")
    void notifyUserJoined_CreatesIndividualNotifications() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.notifyUserJoined(event, participant1);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());

        List<Notification> saved = captor.getAllValues();
        assertEquals(participant1, saved.get(0).getRecipient());
        assertEquals(NotificationService.TYPE_JOIN_SUCCESS, saved.get(0).getType());
        assertEquals("คุณเข้าร่วมกิจกรรมสำเร็จ", saved.get(0).getMessage());

        assertEquals(creator, saved.get(1).getRecipient());
        assertEquals(NotificationService.TYPE_USER_JOINED, saved.get(1).getType());
        assertEquals("มีผู้เข้าร่วมกิจกรรมของคุณ", saved.get(1).getMessage());
    }

    @Test
    @DisplayName("notifyUserLeft สร้างแจ้งเตือนแยกให้ผู้ยกเลิกและผู้สร้างตามข้อความมาตรฐาน")
    void notifyUserLeft_CreatesIndividualNotifications() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.notifyUserLeft(event, participant1);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());

        List<Notification> saved = captor.getAllValues();
        assertEquals(participant1, saved.get(0).getRecipient());
        assertEquals(NotificationService.TYPE_LEAVE_SUCCESS, saved.get(0).getType());
        assertEquals("คุณได้ยกเลิกการเข้าร่วมกิจกรรม", saved.get(0).getMessage());

        assertEquals(creator, saved.get(1).getRecipient());
        assertEquals(NotificationService.TYPE_USER_LEFT, saved.get(1).getType());
        assertEquals("มีผู้ยกเลิกการเข้าร่วมกิจกรรมของคุณ", saved.get(1).getMessage());
    }

    @Test
    @DisplayName("notifyEventCancelled สร้างแจ้งเตือนรายบุคคลให้ผู้เข้าร่วมทุกคน (ไม่ส่งให้ผู้จัด)")
    void notifyEventCancelled_CreatesSeparateRowPerParticipant() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.notifyEventCancelled(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());

        List<Notification> saved = captor.getAllValues();
        assertTrue(saved.stream().allMatch(n -> NotificationService.TYPE_EVENT_CANCELLED.equals(n.getType())));
        assertTrue(saved.stream().allMatch(n -> n.getMessage().contains("ตี๋น้อยที่ซอยห้า")));
        assertEquals(2, saved.stream().map(n -> n.getRecipient().getId()).distinct().count());
    }

    @Test
    @DisplayName("notifyBadgeUpgradedIfEligible สร้างข้อความตรงตามภาพทั้ง 4 ระดับยศ")
    void notifyBadgeUpgradedIfEligible_AllFourTiers() {
        when(notificationRepository.existsByRecipientIdAndTypeAndTitle(anyLong(), anyString(), anyString()))
                .thenReturn(false);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.notifyBadgeUpgradedIfEligible(participant1, event, 0, 1);
        notificationService.notifyBadgeUpgradedIfEligible(participant1, event, 4, 5);
        notificationService.notifyBadgeUpgradedIfEligible(participant1, event, 9, 10);
        notificationService.notifyBadgeUpgradedIfEligible(participant1, event, 19, 20);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(4)).save(captor.capture());

        List<Notification> saved = captor.getAllValues();
        assertEquals("Newly Bee", saved.get(0).getTitle());
        assertEquals("เข้าร่วมกิจกรรมครั้งแรก !\nคุณได้รับยศ Newly Bee", saved.get(0).getMessage());

        assertEquals("Baby Bloom", saved.get(1).getTitle());
        assertEquals("เลื่อนขั้นเป็น Baby Bloom นักท่องโลกกำลังเติบโต", saved.get(1).getMessage());

        assertEquals("Happy Blossom", saved.get(2).getTitle());
        assertEquals("ถึงขั้น Happy Blossom แล้ว ! สนุกกับการทำกิจกรรมต่อไปนะ", saved.get(2).getMessage());

        assertEquals("Super Bouquet", saved.get(3).getTitle());
        assertEquals("ยศ Super Bouquet ขั้นสุดของนักกิจกรรม", saved.get(3).getMessage());
    }

    @Test
    @DisplayName("syncUpcomingEventReminders แจ้งเตือนก่อนเริ่ม 30 นาทีแยกตามรายบุคคล")
    void syncUpcomingEventReminders_NotifiesMembersWithin30Minutes() {
        LocalDateTime startSoon = LocalDateTime.now().plusMinutes(15);
        event.setEventDate(startSoon.toLocalDate());
        event.setEventTime(startSoon.format(DateTimeFormatter.ofPattern("HH:mm")));

        when(eventRepository.findByLifecycleStatus("UPCOMING")).thenReturn(List.of(event));
        when(notificationRepository.existsByRecipientIdAndEventIdAndType(anyLong(), eq(100L), eq(NotificationService.TYPE_EVENT_REMINDER)))
                .thenReturn(false);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.syncUpcomingEventReminders();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(3)).save(captor.capture());
        assertTrue(captor.getAllValues().stream()
                .allMatch(n -> "อีก 30 นาที “ตี๋น้อยที่ซอยห้า” จะเริ่มขึ้น".equals(n.getMessage())));
    }

    @Test
    @DisplayName("markAsRead อัปเดตสถานะอ่านเฉพาะแจ้งเตือนของผู้รับคนนั้น")
    void markAsRead_UpdatesSingleNotification() {
        Notification notification = new Notification(participant1, event, NotificationService.TYPE_JOIN_SUCCESS, "title", "msg");
        notification.setId(50L);
        notification.setIsRead(false);

        when(notificationRepository.findById(50L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        ActionResponse response = notificationService.markAsRead(50L);

        assertTrue(response.isStatus());
        assertTrue(notification.getIsRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("markAllAsRead อ่านเฉพาะแจ้งเตือนของ studentId นั้นและคืนค่า unreadCount = 0")
    @SuppressWarnings("unchecked")
    void markAllAsRead_MarksAllUnreadForUser() {
        Notification unread = new Notification(participant1, event, NotificationService.TYPE_JOIN_SUCCESS, "t", "m");
        unread.setIsRead(false);

        when(userRepository.findByStudentId("6800000001")).thenReturn(Optional.of(participant1));
        when(notificationRepository.findByRecipientStudentIdAndIsReadFalse("6800000001")).thenReturn(List.of(unread));

        ActionResponse response = notificationService.markAllAsRead("6800000001");

        assertTrue(response.isStatus());
        assertTrue(unread.getIsRead());
        Map<String, Object> payload = (Map<String, Object>) response.getData();
        assertEquals(1, payload.get("updatedCount"));
        assertEquals(0L, payload.get("unreadCount"));
    }
}
