package com.example.highpass_backend.dto.chat;

import com.example.highpass_backend.entity.chat.ChatParticipant;
import com.example.highpass_backend.entity.chat.ChatRoom;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ChatRoomResponse {
    private Long id;
    private String name;
    private String type;
    private Long ownerId;
    private Long unreadCount;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Long partnerId;
    private String partnerNickname;
    private String partnerAvatarVisualClassName;
    private List<ChatMessageDto> messages;
    private List<ChatParticipantResponse> participants;

    public ChatRoomResponse(ChatRoom entity, Long currentUserId) {
        this.id = entity.getId();
        ChatRoom.ChatType roomType = entity.getType() != null ? entity.getType() : ChatRoom.ChatType.PERSONAL;
        this.type = roomType.name();
        this.ownerId = entity.getOwnerId();

        this.participants = entity.getParticipants().stream()
                .filter(participant -> participant.getUser() != null)
                .map(ChatParticipantResponse::new)
                .collect(Collectors.toList());

        ChatParticipant myParticipation = entity.getParticipants().stream()
                .filter(participant -> participant.getUser() != null)
                .filter(participant -> participant.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElse(null);

        ChatParticipant partnerParticipation = entity.getParticipants().stream()
                .filter(participant -> participant.getUser() != null)
                .filter(participant -> !participant.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElse(null);

        this.partnerId = partnerParticipation != null ? partnerParticipation.getUser().getId() : null;
        this.partnerNickname = partnerParticipation != null ? partnerParticipation.getUser().getNickname() : null;
        this.partnerAvatarVisualClassName = partnerParticipation != null ? partnerParticipation.getUser().getAvatarVisualClassName() : null;

        LocalDateTime myLastReadAt = myParticipation != null ? myParticipation.getLastReadAt() : null;
        LocalDateTime joinedAt = myParticipation != null ? myParticipation.getJoinedAt() : null;

        boolean isJoined = myParticipation != null && (
                myParticipation.getStatus() == ChatParticipant.ParticipantStatus.JOINED
                        || (roomType == ChatRoom.ChatType.PERSONAL && myParticipation.getStatus() == null)
        );

        this.name = roomType == ChatRoom.ChatType.GROUP
                ? entity.getName() :null;

        this.messages = new ArrayList<>();

        if (!isJoined) {
            this.unreadCount = 0L;
            this.lastMessage = "승인 대기 중입니다.";
            this.lastMessageTime = joinedAt != null ? joinedAt : entity.getCreatedAt();
            return;
        }

        if (entity.getMessages() != null && !entity.getMessages().isEmpty()) {
            this.messages = entity.getMessages().stream()
                    .filter(message -> joinedAt == null || message.getCreatedAt().isAfter(joinedAt))
                    .map(message -> {
                        Long messageSenderId = message.getSender() != null ? message.getSender().getId() : null;
                        long unread = entity.getParticipants().stream()
                                .filter(participant -> participant.getUser() != null)
                                .filter(participant -> !participant.getUser().getId().equals(messageSenderId))
                                .filter(participant -> participant.getStatus() == ChatParticipant.ParticipantStatus.JOINED)
                                .filter(participant -> participant.getLastReadAt() == null || participant.getLastReadAt().isBefore(message.getCreatedAt()))
                                .count();

                        List<Long> readers = entity.getParticipants().stream()
                                .filter(participant -> participant.getUser() != null)
                                .filter(participant -> !participant.getUser().getId().equals(messageSenderId))
                                .filter(participant -> participant.getStatus() == ChatParticipant.ParticipantStatus.JOINED)
                                .filter(participant -> participant.getLastReadAt() != null && !participant.getLastReadAt().isBefore(message.getCreatedAt()))
                                .map(participant -> participant.getUser().getId())
                                .collect(Collectors.toList());

                        return ChatMessageDto.builder()
                                .id(message.getId())
                                .roomId(this.id)
                                .senderId(message.getSender() != null ? message.getSender().getId() : 0L)
                                .senderName(message.getSender() != null ? message.getSender().getNickname() : "알 수 없음")
                                .senderAvatarVisualClassName(message.getSender() != null ? message.getSender().getAvatarVisualClassName() : null)
                                .message(message.getMessage())
                                .createdAt(message.getCreatedAt())
                                .type(message.getType() != null
                                        ? ChatMessageDto.MessageType.valueOf(message.getType().name())
                                        : ChatMessageDto.MessageType.TALK)
                                .unreadCount(unread)
                                .readers(readers)
                                .deleted(message.isDeleted())
                                .build();
                    })
                    .collect(Collectors.toList());

            var lastMessageEntity = entity.getMessages().get(entity.getMessages().size() - 1);
            this.lastMessage = lastMessageEntity.getMessage();
            this.lastMessage = lastMessageEntity.isDeleted() ? "삭제된 메시지입니다. " : lastMessageEntity.getMessage();

            this.unreadCount = entity.getMessages().stream()
                    .filter(message -> message.getSender() != null && !message.getSender().getId().equals(currentUserId))
                    .filter(message -> myLastReadAt == null || message.getCreatedAt().withNano(0).isAfter(myLastReadAt.withNano(0)))
                    .count();
            return;
        }

        this.unreadCount = 0L;
        this.lastMessage = "대화 내역이 없습니다.";
        this.lastMessageTime = entity.getCreatedAt();
    }

}
