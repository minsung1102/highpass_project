package com.example.highpass_backend.service.board;

import com.example.highpass_backend.dto.board.CommentRequest;
import com.example.highpass_backend.dto.board.CommentResponse;
import com.example.highpass_backend.eception.BusinessException;
import com.example.highpass_backend.eception.ErrorCode;
import com.example.highpass_backend.entity.board.Comment;
import com.example.highpass_backend.entity.board.FreeBoard;
import com.example.highpass_backend.entity.board.StudyBoard;
import com.example.highpass_backend.entity.notification.NotificationType;
import com.example.highpass_backend.entity.user.User;
import com.example.highpass_backend.repository.board.CommentRepository;
import com.example.highpass_backend.repository.board.FreeBoardRepository;
import com.example.highpass_backend.repository.board.StudyBoardRepository;
import com.example.highpass_backend.repository.user.UserRepository;
import com.example.highpass_backend.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final FreeBoardRepository freeBoardRepository;
    private final StudyBoardRepository studyBoardRepository;
    private final NotificationService notificationService;

    @Transactional
    public CommentResponse createComment(Long userId, CommentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "인증된 사용자를 찾을 수 없습니다."));

        Comment.TargetType targetType = parseTargetType(request.getTargetType());
        assertTargetExists(targetType, request.getTargetId());

        Comment comment = Comment.builder()
                .user(user)
                .content(request.getContent())
                .targetId(request.getTargetId())
                .targetType(targetType)
                .build();

        Comment savedComment = commentRepository.save(comment);
        sendCommentNotification(user, savedComment);
        return CommentResponse.from(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTarget(Long targetId, String targetType) {
        Comment.TargetType type = parseTargetType(targetType);

        return commentRepository.findByTargetIdAndTargetType(targetId, type)
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = getComment(commentId);
        validateAuthor(userId, comment);
        commentRepository.delete(comment);
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request, Long userId) {
        Comment comment = getComment(commentId);
        validateAuthor(userId, comment);
        comment.updateContent(request.getContent());
        return CommentResponse.from(comment);
    }

    private void sendCommentNotification(User sender, Comment comment) {
        User recipient = null;
        String boardTitle = "";
        Long targetId = comment.getTargetId();
        Comment.TargetType targetType = comment.getTargetType();

        if (targetType == Comment.TargetType.STUDY) {
            StudyBoard study = studyBoardRepository.findById(targetId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 게시글입니다."));
            recipient = study.getUser();
            boardTitle = study.getTitle();
        } else if (targetType == Comment.TargetType.FREE) {
            FreeBoard freeBoard = freeBoardRepository.findById(targetId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 게시글입니다."));
            recipient = freeBoard.getUser();
            boardTitle = freeBoard.getTitle();
        }

        if (recipient != null
                && !recipient.getId().equals(sender.getId())
                && recipient.isCommentNotiOn()) {
            String commentContent = comment.getContent();
            String snippet = commentContent.length() > 10 ? commentContent.substring(0, 10) + "..." : commentContent;
            String message = String.format("%s님이 게시글 [%s]에 댓글을 남겼습니다.", sender.getNickname(), boardTitle);
            notificationService.send(recipient, NotificationType.COMMENT, message, targetId, targetType.name(), snippet, sender.getNickname());
        }
    }

    private void assertTargetExists(Comment.TargetType targetType, Long targetId) {
        boolean exists = switch (targetType) {
            case FREE -> freeBoardRepository.existsById(targetId);
            case STUDY -> studyBoardRepository.existsById(targetId);
        };
        if (!exists) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "댓글 대상 게시글을 찾을 수 없습니다.");
        }
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 댓글입니다."));
    }

    private Comment.TargetType parseTargetType(String targetType) {
        try {
            return Comment.TargetType.valueOf(targetType.toUpperCase());
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "댓글 대상 유형이 올바르지 않습니다.");
        }
    }

    private void validateAuthor(Long userId, Comment comment) {
        if (!comment.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "댓글을 수정하거나 삭제할 권한이 없습니다.");
        }
    }
}
