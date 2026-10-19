package com.tumeet.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_name", nullable = false, length = 150)
    private String eventName;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "event_size", nullable = false)
    private Integer eventSize;

    @Column(name = "current_participants", nullable = false)
    private Integer currentParticipants = 0;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "event_time", nullable = false, length = 20)
    private String eventTime;

    @Column(nullable = false, length = 20)
    private String status = "OPEN";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User creator;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "event_participants",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    public Event() {
    }

    public Event(String eventName, String eventType, Integer eventSize, LocalDate eventDate,
                 String eventTime, User creator) {
        this.eventName = eventName;
        this.eventType = eventType;
        this.eventSize = eventSize;
        this.currentParticipants = 0;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.status = "OPEN";
        this.creator = creator;
    }

    public Event(String eventName, String eventType, String location, Integer eventSize,
                 LocalDate eventDate, String eventTime, User creator) {
        this.eventName = eventName;
        this.eventType = eventType;
        this.location = location;
        this.eventSize = eventSize;
        this.currentParticipants = 0;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.status = "OPEN";
        this.creator = creator;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEventLocation() {
        return location;
    }

    public void setEventLocation(String eventLocation) {
        this.location = eventLocation;
    }

    public Integer getEventSize() {
        return eventSize;
    }

    public void setEventSize(Integer eventSize) {
        this.eventSize = eventSize;
    }

    public Integer getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(Integer currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public void setEventDate(String eventDateStr) {
        if (eventDateStr != null && !eventDateStr.isBlank()) {
            this.eventDate = LocalDate.parse(eventDateStr.trim());
        }
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Set<User> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<User> participants) {
        this.participants = participants != null ? participants : new HashSet<>();
    }

    public void addParticipant(User user) {
        if (user != null && this.participants.add(user)) {
            this.currentParticipants = this.participants.size();
        }
    }

    public void removeParticipant(User user) {
        if (user != null && this.participants.remove(user)) {
            this.currentParticipants = this.participants.size();
        }
    }
}
