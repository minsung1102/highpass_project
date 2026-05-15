package com.example.highpass_backend.service.board;

import com.example.highpass_backend.eception.BusinessException;
import com.example.highpass_backend.eception.ErrorCode;
import com.example.highpass_backend.entity.board.BoardLike;
import com.example.highpass_backend.entity.board.Comment;
import com.example.highpass_backend.entity.user.User;
import com.example.highpass_backend.repository.board.BoardLikeRepository;
import com.example.highpass_backend.repository.board.CommentRepository;
import com.example.highpass_backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardSupportService {
    private final UserRepository userRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final CommentRepository commentRepository;

    public User getAuthenticatedUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "인증된 사용자를 찾을 수 없습니다."));
    }

    public void assertCanModify(Long actorUserId, Long ownerUserId) {
        User actor = getAuthenticatedUser(actorUserId);

        boolean isOwner = ownerUserId.equals(actorUserId);
        boolean isAdmin = actor.getRole() == User.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "게시글을 수정하거나 삭제할 권한이 없습니다.");
        }
    }

    public boolean isLikedByUser(Long currentUserId, BoardLike.TargetType targetType, Long targetId) {
        if (currentUserId == null) {
            return false;
        }

        return boardLikeRepository.existsByUserIdAndTargetTypeAndTargetId(currentUserId, targetType, targetId);
    }

    public void deleteBoardInteractions(Comment.TargetType commentType, BoardLike.TargetType likeType, Long targetId) {
        commentRepository.deleteByTargetTypeAndTargetId(commentType, targetId);
        boardLikeRepository.deleteByTargetTypeAndTargetId(likeType, targetId);
    }
}
