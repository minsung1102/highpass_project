package com.example.highpass_backend.service.user;

import com.example.highpass_backend.entity.user.User;
import com.example.highpass_backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
public class UserPresenceService {

    private final UserRepository userRepository;
    private final ConcurrentMap<String, Long> sessionUsers = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Set<String>> userSessions = new ConcurrentHashMap<>();

    @Transactional
    public void connect(String sessionId, Long userId) {
        if (sessionId == null || userId == null) {
            return;
        }

        sessionUsers.put(sessionId, userId);
        userSessions.computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet()).add(sessionId);
        markSeen(userId);
    }

    @Transactional
    public void disconnect(String sessionId) {
        if (sessionId == null) {
            return;
        }

        Long userId = sessionUsers.remove(sessionId);
        if (userId == null) {
            return;
        }

        Set<String> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
                markSeen(userId);
            }
        }
    }

    @Transactional
    public void markSeen(Long userId) {
        userRepository.findById(userId).ifPresent(User::markSeen);
    }

    @Transactional(readOnly = true)
    public boolean isOnline(Long userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    @Transactional(readOnly = true)
    public Optional<LocalDateTime> getLastSeenAt(Long userId) {
        return userRepository.findById(userId).map(User::getLastSeenAt);
    }

    public Map<Long, Integer> getOnlineUserSessionCounts() {
        Map<Long, Integer> counts = new ConcurrentHashMap<>();
        userSessions.forEach((userId, sessions) -> counts.put(userId, sessions.size()));
        return counts;
    }
}
