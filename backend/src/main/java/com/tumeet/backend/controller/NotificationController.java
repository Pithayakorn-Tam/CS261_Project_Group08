package com.tumeet.backend.controller;

import com.tumeet.backend.dto.ActionResponse;
import com.tumeet.backend.service.EventService;
import com.tumeet.backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final EventService eventService;

    public NotificationController(NotificationService notificationService) {
        this(notificationService, null);
    }

    @Autowired
    public NotificationController(NotificationService notificationService, EventService eventService) {
        this.notificationService = notificationService;
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<ActionResponse> getNotifications(@RequestParam("studentId") String studentId) {
        if (eventService != null) {
            eventService.syncAndCompleteExpiredEvents();
        } else {
            notificationService.syncUpcomingEventReminders();
        }

        ActionResponse response = notificationService.getNotificationsForStudent(studentId);
        return response.isStatus()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ActionResponse> markAsRead(@PathVariable Long notificationId) {
        ActionResponse response = notificationService.markAsRead(notificationId);
        return response.isStatus()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @PutMapping("/read-all")
    public ResponseEntity<ActionResponse> markAllAsRead(@RequestParam("studentId") String studentId) {
        ActionResponse response = notificationService.markAllAsRead(studentId);
        return response.isStatus()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }
}
