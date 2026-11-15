package com.tumeet.backend.repository;

import com.tumeet.backend.model.EventRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRatingRepository extends JpaRepository<EventRating, Long> {

    boolean existsByEventIdAndRaterIdAndRateeId(Long eventId, Long raterId, Long rateeId);

    boolean existsByEventIdAndRaterId(Long eventId, Long raterId);

    List<EventRating> findByEventIdAndRaterId(Long eventId, Long raterId);

    @Query("SELECT DISTINCT r.event.id FROM EventRating r WHERE r.rater.studentId = :studentId")
    List<Long> findReviewedEventIdsByRaterStudentId(@Param("studentId") String studentId);

    @Query("SELECT COUNT(r) FROM EventRating r WHERE r.ratee.id = :rateeId")
    int countByRateeId(@Param("rateeId") Long rateeId);

    @Query("SELECT COALESCE(AVG(CAST(r.score AS double)), 0.0) FROM EventRating r WHERE r.ratee.id = :rateeId")
    double getAverageScoreByRateeId(@Param("rateeId") Long rateeId);
}
