package com.example.highpass_backend.repository.board;

import com.example.highpass_backend.entity.board.FreeBoard;
import com.example.highpass_backend.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FreeBoardRepository extends JpaRepository<FreeBoard, Long> {

    List<FreeBoard> findByUser(User user);

    List<FreeBoard> findByUserId(Long userId);

    long countByUserId(Long userId);

    List<FreeBoard> findByStatusOrStatusIsNullOrderByCreatedAtDesc(FreeBoard.Status status);

    List<FreeBoard> findAllByOrderByCreatedAtDesc();
}
