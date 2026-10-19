package com.tumeet.backend.controller;

import com.tumeet.backend.dto.CreateEventRequest;
import com.tumeet.backend.dto.EventResponse;
import com.tumeet.backend.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody CreateEventRequest request) {
        EventResponse response = eventService.createEvent(request);

        if (response.isStatus()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<EventResponse> getEvents(
            @RequestParam(value = "creatorStudentId", required = false) String creatorStudentId,
            @RequestParam(value = "participantStudentId", required = false) String participantStudentId
    ) {
        if (creatorStudentId != null && !creatorStudentId.isBlank()) {
            EventResponse response = eventService.getEventsByCreator(creatorStudentId);
            return ResponseEntity.ok(response);
        }

        if (participantStudentId != null && !participantStudentId.isBlank()) {
            EventResponse response = eventService.getEventsByParticipant(participantStudentId);
            return ResponseEntity.ok(response);
        }

        EventResponse response = eventService.getAllOpenEvents();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{eventId}/join")
    public ResponseEntity<EventResponse> joinEvent(@PathVariable Long eventId, @RequestBody Map<String, String> body) {
        String studentId = body != null ? body.get("studentId") : null;
        EventResponse response = eventService.joinEvent(eventId, studentId);
        return response.isStatus()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @PostMapping("/{eventId}/leave")
    public ResponseEntity<EventResponse> leaveEvent(@PathVariable Long eventId, @RequestBody Map<String, String> body) {
        String studentId = body != null ? body.get("studentId") : null;
        EventResponse response = eventService.leaveEvent(eventId, studentId);
        return response.isStatus()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
