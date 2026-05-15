package com.example.highpass_backend.repository.chat;

import com.example.highpass_backend.entity.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("select distinct r from ChatRoom r left join fetch r.messages")
    List<ChatRoom> findAllWithMessages();

    @Query("SELECT DISTINCT r FROM ChatRoom r " +
            "WHERE r.type = com.example.highpass_backend.entity.chat.ChatRoom$ChatType.PERSONAL " +
            "AND EXISTS (" +
            "   SELECT 1 FROM ChatParticipant p1 " +
            "   WHERE p1.chatRoom = r AND p1.user.id = :userId" +
            ") " +
            "AND EXISTS (" +
            "   SELECT 1 FROM ChatParticipant p2 " +
            "   WHERE p2.chatRoom = r AND p2.user.id = :partnerId" +
            ") " +
            "AND (" +
            "   SELECT COUNT(p3) FROM ChatParticipant p3 WHERE p3.chatRoom = r" +
            ") = 2")
    Optional<ChatRoom> findExistingChatRoom(@Param("userId") Long userId,
                                               @Param("partnerId") Long partnerId);

    @Query("SELECT DISTINCT r FROM ChatRoom r " +
            "LEFT JOIN FETCH r.participants p " +
            "LEFT JOIN FETCH p.user " +
            "WHERE EXISTS (SELECT p2 FROM ChatParticipant p2 WHERE p2.chatRoom = r AND p2.user.id = :userId)")
    List<ChatRoom> findAllByUserId(@Param("userId") Long userId);
}


