package com.example.highpass_backend.config;

import com.example.highpass_backend.websocket.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private ChatService chatService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        Long roomId = (Long) headerAccessor.getSessionAttributes().get("roomId");

        if (userId != null && roomId != null) {
            chatService.updateOnlineStatus(roomId, userId, true);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        Long roomId = (Long) headerAccessor.getSessionAttributes().get("roomId");

        if (userId != null && roomId != null) {
            chatService.updateOnlineStatus(roomId, userId, false);
            chatService.updateLastReadTime(roomId, userId);
        }
    }
}
