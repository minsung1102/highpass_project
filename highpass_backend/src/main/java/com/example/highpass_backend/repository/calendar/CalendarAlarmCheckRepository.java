package com.example.highpass_backend.repository.calendar;

import com.example.highpass_backend.entity.calendar.CalendarAlarmCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CalendarAlarmCheckRepository extends JpaRepository<CalendarAlarmCheck, Long> {

    Optional<CalendarAlarmCheck> findByUserId(Long userId);
}
