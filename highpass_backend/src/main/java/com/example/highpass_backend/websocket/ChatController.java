package com.example.highpass_backend.websocket;

import com.example.highpass_backend.dto.chat.ChatMessageDto;
import com.example.highpass_backend.dto.chat.ChatRoomResponse;
import com.example.highpass_backend.entity.chat.ChatMessage;
import com.example.highpass_backend.entity.user.User;
import com.example.highpass_backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final SimpMessageSendingOperations messagingTemplate;
    private final UserRepository userRepository;
    @MessageMapping("/chat/message")
    public void message(ChatMessageDto message) {
        chatService.handleMessage(message);
    }

    @MessageMapping("/chat/join")
    public void joinRequest(ChatMessageDto messageDto) {
        chatService.requestJoin(messageDto.getRoomId(), messageDto.getSenderId());

        messagingTemplate.convertAndSend("/sub/user/" + messageDto.getReceiverId() + "/alarm", messageDto);
    }

}