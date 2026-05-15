package com.example.highpass_backend.service.calendar;

import com.example.highpass_backend.dto.calendar.CalendarRequest;
import com.example.highpass_backend.dto.calendar.CalendarResponse;
import com.example.highpass_backend.eception.BusinessException;
import com.example.highpass_backend.eception.ErrorCode;
import com.example.highpass_backend.entity.calendar.Calendar;
import com.example.highpass_backend.entity.calendar.CalendarAlarmCheck;
import com.example.highpass_backend.entity.user.User;
import com.example.highpass_backend.repository.calendar.CalendarAlarmCheckRepository;
import com.example.highpass_backend.repository.calendar.CalendarRepository;
import com.example.highpass_backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CalendarService {
    private final CalendarRepository calendarRepository;
    private final UserRepository userRepository;
    private final CalendarAlarmCheckRepository alarmCheckRepository;

    @Transactional
    public CalendarResponse createCalendar(Long userId, CalendarRequest request) {
        User user = getUser(userId);

        Calendar calendar = Calendar.builder()
                .user(user)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate() != null ? request.getEndDate() : request.getStartDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .title(request.getTitle())
                .content(request.getContent())
                .kind(resolveKind(request.getKind()))
                .build();

        return CalendarResponse.from(calendarRepository.save(calendar));
    }

    @Transactional(readOnly = true)
    public List<CalendarResponse> getCalendarList(Long userId) {
        return calendarRepository.findByUserId(userId).stream()
                .map(CalendarResponse::from)
                .toList();
    }

    @Transactional
    public CalendarResponse updateCalendar(Long currentUserId, Long calendarId, CalendarRequest updateParam) {
        Calendar event = getCalendar(calendarId);
        assertOwner(currentUserId, event);

        event.setTitle(updateParam.getTitle());
        event.setContent(updateParam.getContent());
        event.setStartDate(updateParam.getStartDate());
        event.setEndDate(updateParam.getEndDate() != null ? updateParam.getEndDate() : updateParam.getStartDate());
        event.setStartTime(updateParam.getStartTime());
        event.setEndTime(updateParam.getEndTime());
        event.setKind(resolveKind(updateParam.getKind()));

        return CalendarResponse.from(event);
    }

    @Transactional
    public void deleteCalendar(Long currentUserId, Long calendarId) {
        Calendar event = getCalendar(calendarId);
        assertOwner(currentUserId, event);
        calendarRepository.delete(event);
    }

    @Transactional(readOnly = true)
    public List<CalendarResponse> getTodayAlarms(Long userId) {
        LocalDate today = LocalDate.now();

        Optional<CalendarAlarmCheck> alarmCheck = alarmCheckRepository.findByUserId(userId);
        if (alarmCheck.isPresent() && alarmCheck.get().getLastCheckedDate().equals(today)) {
            return List.of();
        }

        return calendarRepository.findTodayNotifications(userId, today).stream()
                .map(CalendarResponse::from)
                .toList();
    }

    @Transactional
    public void markAlarmAsChecked(Long userId) {
        User user = getUser(userId);
        LocalDate today = LocalDate.now();

        CalendarAlarmCheck alarmCheck = alarmCheckRepository.findByUserId(userId)
                .orElseGet(() -> CalendarAlarmCheck.builder()
                        .user(user)
                        .build());

        alarmCheck.updateDate(today);
        alarmCheckRepository.save(alarmCheck);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "인증된 사용자를 찾을 수 없습니다."));
    }

    private Calendar getCalendar(Long calendarId) {
        return calendarRepository.findById(calendarId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 일정입니다."));
    }

    private void assertOwner(Long currentUserId, Calendar event) {
        if (!event.getUser().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "일정에 접근할 권한이 없습니다.");
        }
    }

    private String resolveKind(String kind) {
        return kind == null || kind.isBlank() ? "general" : kind;
    }
}
