package com.example.highpass_backend.repository.chat;

import com.example.highpass_backend.entity.chat.ChatParticipant;
import com.example.highpass_backend.entity.chat.ChatRoom;
import com.example.highpass_backend.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    List<ChatParticipant> findByUserId(Long userId);

    List<ChatParticipant> findByChatRoomId(Long chatRoomId);

    Optional<ChatParticipant> findByChatRoomIdAndUserId(Long roomId, Long userId);

    List<ChatParticipant> findByChatRoomIdAndStatus(Long roomId, ChatParticipant.ParticipantStatus status);

    boolean existsByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    @Query("SELECT COUNT(p) FROM ChatParticipant p " +
            "WHERE p.chatRoom.id = :roomId " +
            "AND p.user.id != :senderId " +
            "AND p.status = com.example.highpass_backend.entity.chat.ChatParticipant.ParticipantStatus.JOINED")
    int countUnreadParticipants(@Param("roomId") Long roomId, @Param("senderId") Long senderId);

}