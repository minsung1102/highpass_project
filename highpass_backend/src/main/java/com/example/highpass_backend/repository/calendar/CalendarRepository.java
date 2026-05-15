package com.example.highpass_backend.repository.calendar;

import com.example.highpass_backend.entity.calendar.Calendar;
import com.example.highpass_backend.entity.calendar.CalendarAlarmCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CalendarRepository extends JpaRepository<Calendar, Long> {

    List<Calendar> findByUserId(Long userId);

    @Query("SELECT c FROM Calendar c WHERE c.user.id = :userId " +
            "AND (c.startDate = :today OR c.endDate = :today)")
    List<Calendar> findTodayNotifications(@Param("userId") Long userId, @Param("today") LocalDate today);
}


