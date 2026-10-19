package com.tumeet.backend.repository;

import com.tumeet.backend.model.Event;
import com.tumeet.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByCreator(User creator);

    List<Event> findByStatusOrderByEventDateAsc(String status);

    List<Event> findByStatusOrderByIdAsc(String status);
}
