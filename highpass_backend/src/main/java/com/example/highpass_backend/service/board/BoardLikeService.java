package com.example.highpass_backend.service.board;

import com.example.highpass_backend.eception.BusinessException;
import com.example.highpass_backend.eception.ErrorCode;
import com.example.highpass_backend.entity.board.BoardLike;
import com.example.highpass_backend.entity.board.FreeBoard;
import com.example.highpass_backend.entity.board.StudyBoard;
import com.example.highpass_backend.entity.notification.NotificationType;
import com.example.highpass_backend.entity.user.User;
import com.example.highpass_backend.repository.board.BoardLikeRepository;
import com.example.highpass_backend.repository.board.FreeBoardRepository;
import com.example.highpass_backend.repository.board.StudyBoardRepository;
import com.example.highpass_backend.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardLikeService {
    private final BoardLikeRepository boardLikeRepository;
    private final StudyBoardRepository studyBoardRepository;
    private final FreeBoardRepository freeBoardRepository;
    private final NotificationService notificationService;
    private final BoardSupportService boardSupportService;

    @Transactional
    public void toggleLike(Long userId, BoardLike.TargetType targetType, Long targetId) {
        User user = boardSupportService.getAuthenticatedUser(userId);
        BoardTarget target = resolveTarget(targetType, targetId);

        Optional<BoardLike> existingLike = boardLikeRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
        if (existingLike.isPresent()) {
            boardLikeRepository.delete(existingLike.get());
            target.decreaseLikeCount();
            return;
        }

        BoardLike boardLike = BoardLike.builder()
                .user(user)
                .targetType(targetType)
                .targetId(targetId)
                .build();

        boardLikeRepository.save(boardLike);
        target.increaseLikeCount();
        sendLikeNotification(user, targetType, targetId, target);
    }

    private void sendLikeNotification(User sender, BoardLike.TargetType targetType, Long targetId, BoardTarget target) {
        User recipient = target.owner();
        if (recipient != null
                && !recipient.getId().equals(sender.getId())
                && recipient.isLikeNotiOn()) {
            String message = String.format("%s님이 게시글 [%s]에 좋아요를 눌렀습니다.", sender.getNickname(), target.title());
            notificationService.send(recipient, NotificationType.LIKE, message, targetId, targetType.name(), null, sender.getNickname());
        }
    }

    private BoardTarget resolveTarget(BoardLike.TargetType targetType, Long targetId) {
        return switch (targetType) {
            case STUDY -> studyBoardRepository.findById(targetId)
                    .map(study -> new BoardTarget(
                            study.getUser(),
                            study.getTitle(),
                            study::increaseLikeCount,
                            study::decreaseLikeCount
                    ))
                    .orElseThrow(() -> notFound("스터디 게시글"));
            case FREE -> freeBoardRepository.findById(targetId)
                    .map(board -> new BoardTarget(
                            board.getUser(),
                            board.getTitle(),
                            board::increaseLikeCount,
                            board::decreaseLikeCount
                    ))
                    .orElseThrow(() -> notFound("자유 게시글"));
        };
    }

    private BusinessException notFound(String label) {
        return new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 " + label + "입니다.");
    }

    private static class BoardTarget {
        private final User owner;
        private final String title;
        private final Runnable increaseLikeCount;
        private final Runnable decreaseLikeCount;

        private BoardTarget(User owner, String title, Runnable increaseLikeCount, Runnable decreaseLikeCount) {
            this.owner = owner;
            this.title = title;
            this.increaseLikeCount = increaseLikeCount;
            this.decreaseLikeCount = decreaseLikeCount;
        }

        private User owner() {
            return owner;
        }

        private String title() {
            return title;
        }

        void increaseLikeCount() {
            increaseLikeCount.run();
        }

        void decreaseLikeCount() {
            decreaseLikeCount.run();
        }
    }
}
