package com.tumeet.backend.controller;

import com.tumeet.backend.dto.ActionResponse;
import com.tumeet.backend.dto.JoinEventRequest;
import com.tumeet.backend.dto.LeaveEventRequest;
import com.tumeet.backend.model.EventParticipant;
import com.tumeet.backend.service.EventParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventParticipantController {

    private final EventParticipantService eventParticipantService;

    @Autowired
    public EventParticipantController(EventParticipantService eventParticipantService) {
        this.eventParticipantService = eventParticipantService;
    }

    @PostMapping("/join")
    public ResponseEntity<ActionResponse> joinEvent(@RequestBody JoinEventRequest request) {
        ActionResponse response = eventParticipantService.joinEvent(request);
        if (response.isStatus()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/leave")
    public ResponseEntity<ActionResponse> leaveEvent(@RequestBody LeaveEventRequest request) {
        ActionResponse response = eventParticipantService.leaveEvent(request);
        if (response.isStatus()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{eventId}/participants")
    public ResponseEntity<List<EventParticipant>> getParticipants(@PathVariable Long eventId) {
        List<EventParticipant> participants = eventParticipantService.getParticipantsByEventId(eventId);
        return ResponseEntity.ok(participants);
    }
}