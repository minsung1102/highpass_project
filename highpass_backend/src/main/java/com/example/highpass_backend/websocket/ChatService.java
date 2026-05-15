package com.example.highpass_backend.websocket;

import com.example.highpass_backend.dto.chat.ChatMessageDto;
import com.example.highpass_backend.dto.chat.ChatParticipantResponse;
import com.example.highpass_backend.dto.chat.ChatRoomReadStateResponse;
import com.example.highpass_backend.dto.chat.ChatRoomResponse;
import com.example.highpass_backend.dto.chat.MessageReadStateResponse;
import com.example.highpass_backend.dto.chat.StudyChatJoinResponse;
import com.example.highpass_backend.dto.notification.ChatNotificationDto;
import com.example.highpass_backend.dto.notification.NotificationResponse;
import com.example.highpass_backend.entity.board.StudyBoard;
import com.example.highpass_backend.entity.chat.ChatMessage;
import com.example.highpass_backend.entity.chat.ChatParticipant;
import com.example.highpass_backend.entity.chat.ChatRoom;
import com.example.highpass_backend.entity.notification.Notification;
import com.example.highpass_backend.entity.notification.NotificationType;
import com.example.highpass_backend.entity.user.User;
import com.example.highpass_backend.repository.board.StudyBoardRepository;
import com.example.highpass_backend.repository.chat.ChatMessageRepository;
import com.example.highpass_backend.repository.chat.ChatParticipantRepository;
import com.example.highpass_backend.repository.chat.ChatRoomRepository;
import com.example.highpass_backend.repository.notification.NotificationRepository;
import com.example.highpass_backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final SimpMessageSendingOperations messagingTemplate;
    private final StudyBoardRepository studyBoardRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public ChatRoom createOneToOneRoom(Long userId, Long partnerId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        User partner = userRepository.findById(partnerId).orElseThrow(() -> new RuntimeException("존재하지 않는 상대방 계정입니다."));

        ChatRoom existingRoom = chatRoomRepository.findExistingChatRoom(userId, partnerId)
                .orElseGet(() -> {
                    ChatRoom room = ChatRoom.builder()
                            .name("")
                            .type(ChatRoom.ChatType.PERSONAL)
                            .isApprovalRequired(false)
                            .build();
                    chatRoomRepository.save(room);

                    ChatParticipant userInfo = ChatParticipant.builder()
                            .chatRoom(room)
                            .user(user)
                            .roomNickname(partner.getNickname())
                            .status(ChatParticipant.ParticipantStatus.JOINED)
                            .build();

                    ChatParticipant partnerInfo = ChatParticipant.builder()
                            .chatRoom(room)
                            .user(partner)
                            .roomNickname(user.getNickname())
                            .status(ChatParticipant.ParticipantStatus.JOINED)
                            .build();

                    chatParticipantRepository.save(userInfo);
                    chatParticipantRepository.save(partnerInfo);

                    room.getParticipants().add(userInfo);
                    room.getParticipants().add(partnerInfo);

                    return room;
                });

        return existingRoom;
    }

    @Transactional
    public ChatRoom createGroupChatRoom (String name, Long ownerId, boolean isApprovalRequired) {
        User Owner = userRepository.findById(ownerId).orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다. "));

        ChatRoom groupChatRoom = ChatRoom.builder()
                .name(name)
                .type(ChatRoom.ChatType.GROUP)
                .ownerId(ownerId)
                .isApprovalRequired(isApprovalRequired)
                .build();

        chatRoomRepository.save(groupChatRoom);

        ChatParticipant ownerParticipant = groupChatRoom.addParticipant(Owner,true);
        chatParticipantRepository.save(ownerParticipant);

        return groupChatRoom;
    }

    @Transactional
    public List<ChatParticipantResponse> getPendingParticipants (Long roomId, Long ownerId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("존재하지 않는 채팅방입니다."));

        if(!chatRoom.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        return chatParticipantRepository.findByChatRoomIdAndStatus(roomId, ChatParticipant.ParticipantStatus.PENDING)
                .stream()
                .map(ChatParticipantResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void rejectParticipant(Long roomId, Long ownerId, Long targetUserId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("존재하지 않는 채팅방입니다."));
        if (!chatRoom.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("권한이 없습니다.");
        }
        ChatParticipant participant = chatParticipantRepository.findByChatRoomIdAndUserId(roomId, targetUserId).orElseThrow(() -> new RuntimeException("신청 내역이 없습니다 "));
        User targetUser = participant.getUser();

        chatParticipantRepository.delete(participant);

        Notification rejectNotification = Notification.builder()
                .recipient(targetUser)
                .senderNickname("")
                .type(NotificationType.CHAT)
                .message("'" + chatRoom.getName() + "' 채팅방 참여가 거절되었습니다.")
                .content(chatRoom.getName())
                .targetId(roomId)
                .build();
        notificationRepository.save(rejectNotification);
        messagingTemplate.convertAndSend("/sub/notifications/" + targetUserId, NotificationResponse.from(rejectNotification));
    }

    @Transactional
    public void cancelJoinRequest(Long roomId, Long userId) {
        ChatParticipant participant = chatParticipantRepository.findByChatRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new RuntimeException("참여 요청 내역이 없습니다."));

        if (participant.getStatus() != ChatParticipant.ParticipantStatus.PENDING) {
            throw new RuntimeException("승인 대기 중인 요청만 취소할 수 있습니다.");
        }

        chatParticipantRepository.delete(participant);
    }

    @Transactional
    public void updateOnlineStatus(Long roomId, Long userId, boolean isOnline) {
        ChatParticipant participant = chatParticipantRepository.findByChatRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new RuntimeException("참여 정보 없음"));

        participant.setOnline(isOnline);
    }

    @Transactional
    public void updateChatRoomName(Long roomId, Long ownerId, String newName) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 채팅방입니다."));

        if (!room.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("방장만 이름을 변경할 수 있습니다.");
        }

        room.setName(newName);

        ChatMessage noticeMessage = ChatMessage.builder()
                .chatRoom(room)
                .sender(null)
                .message("채팅방 이름이 '" + newName + "'으로 변경되었습니다.")
                .type(ChatMessage.MessageType.NOTICE)
                .build();
        chatMessageRepository.save(noticeMessage);

        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId,
                ChatMessageDto.builder()
                        .type(ChatMessageDto.MessageType.NOTICE)
                        .roomId(roomId)
                        .id(noticeMessage.getId())
                        .createdAt(noticeMessage.getCreatedAt())
                        .message(noticeMessage.getMessage())
                        .roomName(newName)
                        .build());
    }

    @Transactional
    public void transferOwner(Long roomId, Long currentOwnerId, Long newOwnerId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 채팅방입니다."));

        if (!room.getOwnerId().equals(currentOwnerId)) {
            throw new RuntimeException("방장만 위임할 수 있습니다.");
        }

        ChatParticipant currentOwnerParticipant = chatParticipantRepository.findByChatRoomIdAndUserId(roomId, currentOwnerId)
                .orElseThrow(() -> new RuntimeException("방장 정보를 찾을 수 없습니다."));
        ChatParticipant newOwnerParticipant = chatParticipantRepository.findByChatRoomIdAndUserId(roomId, newOwnerId)
                .orElseThrow(() -> new RuntimeException("해당 참여자를 찾을 수 없습니다."));

        currentOwnerParticipant.setOwner(false);
        newOwnerParticipant.setOwner(true);
        room.setOwnerId(newOwnerId);

        ChatMessage noticeMessage = ChatMessage.builder()
                .chatRoom(room)
                .sender(null)
                .message(newOwnerParticipant.getUser().getNickname() + "님이 새로운 방장이 되었습니다.")
                .type(ChatMessage.MessageType.NOTICE)
                .build();
        chatMessageRepository.save(noticeMessage);

        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId,
                ChatMessageDto.builder()
                        .type(ChatMessageDto.MessageType.NOTICE)
                        .roomId(roomId)
                        .id(noticeMessage.getId())
                        .createdAt(noticeMessage.getCreatedAt())
                        .message(noticeMessage.getMessage())
                        .newOwnerId(newOwnerId)
                        .build());
    }

    @Transactional
    public void handleMessage(ChatMessageDto messageDto) {
        if (ChatMessageDto.MessageType.ENTER.equals(messageDto.getType())) {
            messageDto.setCreatedAt(LocalDateTime.now());
            messageDto.setEnterMessage();
            updateLastReadTime(messageDto.getRoomId(), messageDto.getSenderId());
            messageDto.setUnreadCount(0L);
        } else if (ChatMessageDto.MessageType.TALK.equals(messageDto.getType())) {
            messageDto.setCreatedAt(LocalDateTime.now());
            int unreadCount = chatParticipantRepository.countUnreadParticipants(messageDto.getRoomId(), messageDto.getSenderId());
            messageDto.setUnreadCount((long) unreadCount);

            saveMessageToDb(messageDto);
        }
        messagingTemplate.convertAndSend("/sub/chat/room/" + messageDto.getRoomId(), messageDto);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getMyRooms(Long userId) {
        return chatRoomRepository.findAllByUserId(userId).stream()
                .map(room -> new ChatRoomResponse(room, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChatRoomReadStateResponse getReadState(Long roomId, Long userId, List<Long> messageIds) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 채팅방입니다."));

        ChatParticipant myParticipant = room.getParticipants().stream()
                .filter(participant -> participant.getUser() != null)
                .filter(participant -> participant.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("참여 중인 채팅방이 아닙니다."));

        if (myParticipant.getStatus() == ChatParticipant.ParticipantStatus.PENDING) {
            throw new RuntimeException("승인 대기 중인 채팅방은 읽음 상태를 조회할 수 없습니다.");
        }

        Set<Long> targetMessageIds = messageIds == null ? Set.of() : new HashSet<>(messageIds);

        List<MessageReadStateResponse> states = room.getMessages().stream()
                .filter(message -> message.getId() != null)
                .filter(message -> targetMessageIds.isEmpty() || targetMessageIds.contains(message.getId()))
                .map(message -> {
                    Long senderId = message.getSender() != null ? message.getSender().getId() : null;
                    long unread = room.getParticipants().stream()
                            .filter(participant -> participant.getUser() != null)
                            .filter(participant -> !participant.getUser().getId().equals(senderId))
                            .filter(participant -> participant.getStatus() == ChatParticipant.ParticipantStatus.JOINED)
                            .filter(participant -> participant.getLastReadAt() == null || participant.getLastReadAt().isBefore(message.getCreatedAt()))
                            .count();

                    List<Long> readers = room.getParticipants().stream()
                            .filter(participant -> participant.getUser() != null)
                            .filter(participant -> !participant.getUser().getId().equals(senderId))
                            .filter(participant -> participant.getStatus() == ChatParticipant.ParticipantStatus.JOINED)
                            .filter(participant -> participant.getLastReadAt() != null && !participant.getLastReadAt().isBefore(message.getCreatedAt()))
                            .map(participant -> participant.getUser().getId())
                            .collect(Collectors.toList());

                    return new MessageReadStateResponse(message.getId(), unread, readers);
                })
                .collect(Collectors.toList());

        return new ChatRoomReadStateResponse(roomId, states);
    }

    private void saveMessageToDb(ChatMessageDto dto) {
        ChatRoom chatRoom = chatRoomRepository.findById(dto.getRoomId()).orElseThrow(() -> new RuntimeException("존재하지 않는 채팅방입니다."));

        User sender = userRepository.findById(dto.getSenderId()).orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .message(dto.getMessage())
                .type(ChatMessage.MessageType.TALK)
                .build();
        ChatMessage savedMessage = chatMessageRepository.saveAndFlush(chatMessage);
        dto.setId(savedMessage.getId());
        if (savedMessage.getCreatedAt() != null) {
            dto.setCreatedAt(savedMessage.getCreatedAt());
        }

    }

    @Transactional
    public void updateLastReadTime(Long roomId, Long userId) {
        try {
            ChatParticipant participant = chatParticipantRepository
                    .findByChatRoomIdAndUserId(roomId, userId)
                    .orElseThrow(() -> new RuntimeException("참여 정보를 찾을 수 없습니다."));

            participant.updateLastRead();
            chatParticipantRepository.saveAndFlush(participant);

            messagingTemplate.convertAndSend("/sub/chat/room/" + roomId,
                    ChatMessageDto.builder()
                            .type(ChatMessageDto.MessageType.READ)
                            .roomId(roomId)
                            .senderId(userId)
                            .build());
        } catch (Exception e) {
            log.warn("읽음 처리 실패: {}", e.getMessage());
        }
    }

    @Transactional
    public void requestJoin (Long roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("존재하지 않는 채팅방입니다. "));
        if (chatRoom.getOwnerId() == null) {
            throw new RuntimeException("채팅방 방장 정보가 없습니다.");
        }
        if (chatParticipantRepository.existsByChatRoomIdAndUserId(roomId, userId)) {
            throw new RuntimeException("이미 참여 중이거나 승인 대기 중인 방입니다.");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다. "));

        ChatParticipant participant = chatRoom.addParticipant(user, false);
        chatParticipantRepository.save(participant);

        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId,
                ChatMessageDto.builder()
                        .type(ChatMessageDto.MessageType.JOIN_REQUEST)
                        .roomId(roomId)
                        .senderId(userId)
                        .senderName(user.getNickname())
                        .build());

        User roomOwner = userRepository.findById(chatRoom.getOwnerId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        Notification notification = Notification.builder()
                .recipient(roomOwner)
                .senderNickname(user.getNickname())
                .type(NotificationType.CHAT)
                .targetId(roomId)
                .targetType("CHAT")
                .message(user.getNickname() + "님이 '" + chatRoom.getName() + "' 채팅방 참여를 요청했습니다.")
                .content(chatRoom.getName())
                .build();

        notificationRepository.save(notification);
        messagingTemplate.convertAndSend(
                "/sub/notifications/" + chatRoom.getOwnerId(),
                NotificationResponse.from(notification)
        );}


    @Transactional
    public void approveParticipant(Long roomId, Long ownerId, Long targetUserId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 채팅방입니다."));
        if (!chatRoom.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("권한이 없습니다.");
        }
        ChatParticipant participant = chatParticipantRepository
                .findByChatRoomIdAndUserId(roomId, targetUserId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 채팅방 또는 사용자입니다."));

        participant.setStatus(ChatParticipant.ParticipantStatus.JOINED);

        String nickname = participant.getUser().getNickname();

        ChatMessage enterMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(participant.getUser())
                .message(nickname + "님이 입장하셨습니다.")
                .type(ChatMessage.MessageType.ENTER)
                .build();
        chatMessageRepository.save(enterMessage);

        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId,
                ChatMessageDto.builder()
                        .type(ChatMessageDto.MessageType.ENTER)
                        .roomId(roomId)
                        .senderId(targetUserId)
                        .senderName(nickname)
                        .message(nickname + "님이 입장하셨습니다.")
                        .createdAt(enterMessage.getCreatedAt())
                        .build());

        Notification approveNotification = Notification.builder()
                .recipient(participant.getUser())
                .senderNickname("")
                .type(NotificationType.CHAT)
                .message("'" + chatRoom.getName() + "' 채팅방 참여가 승인되었습니다.")
                .content(chatRoom.getName())
                .targetId(roomId)
                .targetType("CHAT")
                .build();
        notificationRepository.save(approveNotification);
        messagingTemplate.convertAndSend("/sub/notifications/" + targetUserId, NotificationResponse.from(approveNotification));
    }

    @Transactional
    public void kickParticipant(Long roomId, Long ownerId, Long targetUserId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 채팅방입니다."));

        if (!room.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("방장만 강퇴할 수 있습니다.");
        }

        if (targetUserId.equals(ownerId)) {
            throw new RuntimeException("방장은 강퇴할 수 없습니다.");
        }

        ChatParticipant target = chatParticipantRepository
                .findByChatRoomIdAndUserId(roomId, targetUserId)
                .orElseThrow(() -> new RuntimeException("해당 참여자가 없습니다."));

        String nickname = target.getUser().getNickname();
        chatParticipantRepository.delete(target);

        ChatMessage kickMessage = ChatMessage.builder()
                .chatRoom(room)
                .sender(target.getUser())
                .message(nickname + "님이 강퇴되었습니다.")
                .type(ChatMessage.MessageType.QUIT)
                .build();
        chatMessageRepository.save(kickMessage);

        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId,
                ChatMessageDto.builder()
                        .type(ChatMessageDto.MessageType.QUIT)
                        .roomId(roomId)
                        .senderName(nickname)
                        .message(nickname + "님이 강퇴되었습니다.")
                        .build());
    }

    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 채팅방입니다."));

        ChatParticipant participant = chatParticipantRepository
                .findByChatRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new RuntimeException("참여 중인 방이 아닙니다."));

        String nickname = participant.getUser().getNickname();

        chatParticipantRepository.delete(participant);

        chatParticipantRepository.flush();

        ChatMessage leaveMessage = ChatMessage.builder()
                .chatRoom(room)
                .sender(participant.getUser())
                .message(nickname + "님이 나가셨습니다.")
                .type(ChatMessage.MessageType.QUIT)
                .build();
        chatMessageRepository.save(leaveMessage);


        List<ChatParticipant> remaining = chatParticipantRepository.findByChatRoomId(roomId);
        boolean ownerLeaving = room.getOwnerId() != null && room.getOwnerId().equals(userId);

        if (remaining.isEmpty()) {
            studyBoardRepository.findByChatRoomId(room.getId())
                    .ifPresent(study -> study.setChatRoom(null));
            chatRoomRepository.delete(room);
        } else if (ownerLeaving) {
            ChatParticipant newOwner = remaining.get(0);
            newOwner.setOwner(true);
            room.setOwnerId(newOwner.getUser().getId());
        }

        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId,
                ChatMessageDto.builder()
                        .type(ChatMessageDto.MessageType.QUIT)
                        .roomId(roomId)
                        .senderName(nickname)
                        .message(nickname + "님이 나가셨습니다.")
                        .build());
    }

    @Transactional
    public StudyChatJoinResponse joinStudyChat(Long studyId, Long userId) {
        StudyBoard study = studyBoardRepository.findById(studyId)
                .orElseThrow(() -> new RuntimeException("게시글이 없습니다."));

        if (study.getChatRoom() == null) {
            ChatRoom newRoom = ChatRoom.builder()
                    .name(study.getTitle() )
                    .type(ChatRoom.ChatType.GROUP)
                    .isApprovalRequired(true)
                    .ownerId(study.getUser().getId())
                    .build();
            chatRoomRepository.save(newRoom);
            ChatParticipant ownerParticipant = newRoom.addParticipant(study.getUser(), true);
            chatParticipantRepository.save(ownerParticipant);
            study.setChatRoom(newRoom);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저가 없습니다."));

        ChatRoom room = study.getChatRoom();
        if (room.getType() == null) {
            room.setType(ChatRoom.ChatType.GROUP);
        }
        if (room.getName() == null || room.getName().isBlank()) {
            room.setName(study.getTitle());
        }
        if (room.getOwnerId() == null) {
            room.setOwnerId(study.getUser().getId());
        }

        Optional<ChatParticipant> existing = chatParticipantRepository.findByChatRoomIdAndUserId(room.getId(), userId);
        if (existing.isPresent()) {
            ChatParticipant.ParticipantStatus status = existing.get().getStatus() != null
                    ? existing.get().getStatus()
                    : ChatParticipant.ParticipantStatus.JOINED;
            return new StudyChatJoinResponse(room.getId(), status.name());
        }

        boolean isOwner = study.getUser().getId().equals(userId);

        ChatParticipant participant = ChatParticipant.builder()
                .chatRoom(room)
                .user(user)
                .isOwner(isOwner)
                .status(isOwner ? ChatParticipant.ParticipantStatus.JOINED : ChatParticipant.ParticipantStatus.PENDING)
                .build();

        chatParticipantRepository.save(participant);

        if (participant.getStatus() == ChatParticipant.ParticipantStatus.JOINED) {
            messagingTemplate.convertAndSend("/sub/chat/room/" + room.getId(),
                    user.getNickname() + "님이 입장하셨습니다.");
        }


        if (participant.getStatus() == ChatParticipant.ParticipantStatus.PENDING) {
            messagingTemplate.convertAndSend("/sub/chat/room/" + room.getId(),
                    ChatMessageDto.builder()
                            .type(ChatMessageDto.MessageType.JOIN_REQUEST)
                            .roomId(room.getId())
                            .senderId(userId)
                            .senderName(user.getNickname())
                            .build());

            User roomOwner = userRepository.findById(room.getOwnerId())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

            Notification notification = Notification.builder()
                    .recipient(roomOwner)
                    .senderNickname(user.getNickname())
                    .type(NotificationType.CHAT)
                    .targetId(room.getId())
                    .targetType("CHAT")
                    .message(user.getNickname() + "님이 '" + room.getName() + "' 채팅방 참여를 요청했습니다.")
                    .content(room.getName())
                    .build();

            notificationRepository.save(notification);
            messagingTemplate.convertAndSend("/sub/notifications/" + room.getOwnerId(),
                    NotificationResponse.from(notification));
        }

        return new StudyChatJoinResponse(room.getId(), participant.getStatus().name());
    }

    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("메시지를 찾을 수 없습니다."));
        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("본인 메시지만 삭제할 수 있습니다.");
        }
        message.markAsDeleted();

        ChatMessageDto deleteDto = ChatMessageDto.builder()
                .id(messageId)
                .roomId(message.getChatRoom().getId())
                .type(ChatMessageDto.MessageType.valueOf("DELETE"))
                .deleted(true)
                .build();

        messagingTemplate.convertAndSend(
                "/sub/chat/room/" + message.getChatRoom().getId(),
                deleteDto
        );
    }

}

