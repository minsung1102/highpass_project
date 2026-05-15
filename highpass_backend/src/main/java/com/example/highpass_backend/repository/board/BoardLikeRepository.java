package com.example.highpass_backend.repository.board;

import com.example.highpass_backend.entity.board.BoardLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {
    Optional<BoardLike> findByUserIdAndTargetTypeAndTargetId(Long userId, BoardLike.TargetType targetType, Long targetId);

    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, BoardLike.TargetType targetType, Long targetId);

    void deleteByTargetTypeAndTargetId(BoardLike.TargetType targetType, Long targetId);
}
