package com.example.highpass_backend.websocket;

import com.example.highpass_backend.service.user.UserPresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class UserPresenceEventListener {

    private final UserPresenceService userPresenceService;

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String userIdHeader = accessor.getFirstNativeHeader("userId");

        if (userIdHeader == null || userIdHeader.isBlank()) {
            return;
        }

        try {
            userPresenceService.connect(sessionId, Long.parseLong(userIdHeader));
        } catch (NumberFormatException ignored) {
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        userPresenceService.disconnect(event.getSessionId());
    }
}
