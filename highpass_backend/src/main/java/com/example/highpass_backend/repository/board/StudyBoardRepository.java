package com.example.highpass_backend.repository.board;

import com.example.highpass_backend.entity.board.StudyBoard;
import com.example.highpass_backend.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyBoardRepository extends JpaRepository<StudyBoard, Long> {

    List<StudyBoard> findByUser(User user);

    List<StudyBoard> findByUserId(Long userId);

    long countByUserId(Long userId);

    List<StudyBoard> findByStatusOrStatusIsNullOrderByCreatedAtDesc(StudyBoard.Status status);

    List<StudyBoard> findAllByOrderByCreatedAtDesc();

    Optional<StudyBoard> findByChatRoomId(Long chatRoomId);
}
