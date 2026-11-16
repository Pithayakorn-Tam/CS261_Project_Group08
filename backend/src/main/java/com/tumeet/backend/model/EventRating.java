package com.tumeet.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "event_ratings",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"event_id", "rater_id", "ratee_id"})
        }
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "event", "rater", "ratee"})
public class EventRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rater_id", nullable = false)
    private User rater; // ผู้ให้คะแนน

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ratee_id", nullable = false)
    private User ratee; // ผู้รับคะแนน

    @Column(nullable = false)
    private Integer score; // 1 ถึง 5

    @Column(name = "q1_score")
    private Integer q1Score;

    @Column(name = "q2_score")
    private Integer q2Score;

    @Column(name = "q3_score")
    private Integer q3Score;

    @Column(name = "q4_score")
    private Integer q4Score;

    @Column(name = "comment", length = 500)
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public EventRating() {
    }

    public EventRating(Event event, User rater, User ratee, Integer score) {
        this.event = event;
        this.rater = rater;
        this.ratee = ratee;
        this.score = score;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public User getRater() {
        return rater;
    }

    public void setRater(User rater) {
        this.rater = rater;
    }

    public User getRatee() {
        return ratee;
    }

    public void setRatee(User ratee) {
        this.ratee = ratee;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getQ1Score() {
        return q1Score;
    }

    public void setQ1Score(Integer q1Score) {
        this.q1Score = q1Score;
    }

    public Integer getQ2Score() {
        return q2Score;
    }

    public void setQ2Score(Integer q2Score) {
        this.q2Score = q2Score;
    }

    public Integer getQ3Score() {
        return q3Score;
    }

    public void setQ3Score(Integer q3Score) {
        this.q3Score = q3Score;
    }

    public Integer getQ4Score() {
        return q4Score;
    }

    public void setQ4Score(Integer q4Score) {
        this.q4Score = q4Score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
