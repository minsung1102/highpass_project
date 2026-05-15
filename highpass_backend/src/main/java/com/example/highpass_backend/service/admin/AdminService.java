package com.example.highpass_backend.service.admin;

import com.example.highpass_backend.dto.admin.AdminPostResponse;
import com.example.highpass_backend.dto.admin.AdminReportChatMessageResponse;
import com.example.highpass_backend.dto.admin.AdminReportResponse;
import com.example.highpass_backend.dto.admin.AdminUserResponse;
import com.example.highpass_backend.dto.user.UserDisplayName;
import com.example.highpass_backend.eception.BusinessException;
import com.example.highpass_backend.eception.ErrorCode;
import com.example.highpass_backend.entity.auth.OAuth2Account;
import com.example.highpass_backend.entity.board.Comment;
import com.example.highpass_backend.entity.board.FreeBoard;
import com.example.highpass_backend.entity.board.StudyBoard;
import com.example.highpass_backend.entity.chat.ChatMessage;
import com.example.highpass_backend.entity.chat.ChatParticipant;
import com.example.highpass_backend.entity.notification.NotificationType;
import com.example.highpass_backend.entity.report.Report;
import com.example.highpass_backend.entity.user.User;
import com.example.highpass_backend.repository.auth.OAuth2AccountRepository;
import com.example.highpass_backend.repository.board.CommentRepository;
import com.example.highpass_backend.repository.board.FreeBoardRepository;
import com.example.highpass_backend.repository.board.StudyBoardRepository;
import com.example.highpass_backend.repository.chat.ChatMessageRepository;
import com.example.highpass_backend.repository.chat.ChatParticipantRepository;
import com.example.highpass_backend.repository.chat.ChatRoomRepository;
import com.example.highpass_backend.repository.report.ReportRepository;
import com.example.highpass_backend.repository.user.UserRepository;
import com.example.highpass_backend.service.notification.NotificationService;
import com.example.highpass_backend.service.user.UserPresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final FreeBoardRepository freeBoardRepository;
    private final StudyBoardRepository studyBoardRepository;
    private final CommentRepository commentRepository;
    private final OAuth2AccountRepository oauth2AccountRepository;
    private final UserPresenceService userPresenceService;
    private final ReportRepository reportRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getUsers(Long adminUserId) {
        requireAdmin(adminUserId);
        return userRepository.findByRoleNotOrderByCreatedAtDesc(User.Role.ADMIN).stream()
                .map(this::toAdminUserResponse)
                .toList();
    }

    @Transactional
    public AdminUserResponse updateUserStatus(Long adminUserId, Long userId, String status) {
        requireAdmin(adminUserId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        user.updateStatus(parseUserStatus(status));

        return toAdminUserResponse(user);
    }

    @Transactional(readOnly = true)
    public List<AdminPostResponse> getPosts(Long adminUserId) {
        requireAdmin(adminUserId);
        return Stream.concat(
                        freeBoardRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toFreePostResponse),
                        studyBoardRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toStudyPostResponse)
                )
                .sorted(Comparator.comparing(AdminPostResponse::createdAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }

    @Transactional
    public AdminPostResponse updatePostStatus(Long adminUserId, String postId, String status) {
        requireAdmin(adminUserId);
        String[] parts = postId.split("-", 2);
        if (parts.length == 2 && "study".equalsIgnoreCase(parts[0])) {
            return updateStudyStatus(parseLong(parts[1], "게시글 ID가 올바르지 않습니다."), status);
        }
        if (parts.length == 2 && "free".equalsIgnoreCase(parts[0])) {
            return updateFreeStatus(parseLong(parts[1], "게시글 ID가 올바르지 않습니다."), status);
        }

        Long numericId = parseLong(postId, "게시글 ID가 올바르지 않습니다.");
        return freeBoardRepository.findById(numericId)
                .map(board -> {
                    board.updateStatus(parseFreeStatus(status));
                    return toFreePostResponse(board);
                })
                .orElseGet(() -> updateStudyStatus(numericId, status));
    }

    @Transactional(readOnly = true)
    public List<AdminReportResponse> getReports(Long adminUserId) {
        requireAdmin(adminUserId);
        return reportRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toAdminReportResponse)
                .toList();
    }

    @Transactional
    public AdminReportResponse updateReportStatus(Long adminUserId, String reportId, String status, String message) {
        requireAdmin(adminUserId);
        Report report = reportRepository.findById(parseLong(reportId, "신고 ID가 올바르지 않습니다."))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고를 찾을 수 없습니다."));
        Report.Status nextStatus = parseReportStatus(status);
        String responseMessage = message == null ? "" : message.trim();
        report.updateStatus(nextStatus);
        if (!responseMessage.isBlank()) {
            report.respond(responseMessage);
        }
        sendReportProcessedNotification(report, nextStatus, responseMessage);
        return toAdminReportResponse(report);
    }

    private void sendReportProcessedNotification(Report report, Report.Status status, String responseMessage) {
        if (status == Report.Status.PENDING || report.getReporter() == null) {
            return;
        }

        String resultLabel = status == Report.Status.RESOLVED ? "승인" : "반려";
        String typeLabel = report.getTargetType() == Report.TargetType.INQUIRY ? "문의" : "신고";
        notificationService.send(
                report.getReporter(),
                NotificationType.REPORT,
                typeLabel + "가 " + resultLabel + " 처리되었습니다.",
                report.getId(),
                "REPORT",
                responseMessage.isBlank() ? report.getTargetLabel() : responseMessage,
                "관리자"
        );
    }

    private void requireAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (user.getRole() != User.Role.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
        }
    }

    private AdminPostResponse updateFreeStatus(Long postId, String status) {
        FreeBoard board = freeBoardRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "자유 게시글을 찾을 수 없습니다."));
        board.updateStatus(parseFreeStatus(status));
        return toFreePostResponse(board);
    }

    private AdminPostResponse updateStudyStatus(Long postId, String status) {
        StudyBoard study = studyBoardRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "스터디 게시글을 찾을 수 없습니다."));
        study.updateStatus(parseStudyStatus(status));
        return toStudyPostResponse(study);
    }

    private AdminPostResponse toFreePostResponse(FreeBoard board) {
        FreeBoard.Status status = board.getStatus() == null ? FreeBoard.Status.VISIBLE : board.getStatus();
        return new AdminPostResponse(
                "free-" + board.getId(),
                "free",
                board.getTitle(),
                board.getContent(),
                String.valueOf(board.getUser().getId()),
                UserDisplayName.nickname(board.getUser()),
                status.name().toLowerCase(),
                board.getCreatedAt(),
                board.getViewCount(),
                commentRepository.findByTargetIdAndTargetType(board.getId(), Comment.TargetType.FREE).size(),
                reportRepository.countByTargetTypeAndTargetId(Report.TargetType.POST, "free-" + board.getId())
        );
    }

    private AdminPostResponse toStudyPostResponse(StudyBoard study) {
        StudyBoard.Status status = study.getStatus() == null ? StudyBoard.Status.VISIBLE : study.getStatus();
        return new AdminPostResponse(
                "study-" + study.getId(),
                "study",
                study.getTitle(),
                study.getContent(),
                String.valueOf(study.getUser().getId()),
                UserDisplayName.nickname(study.getUser()),
                status.name().toLowerCase(),
                study.getCreatedAt(),
                study.getViewCount(),
                commentRepository.findByTargetIdAndTargetType(study.getId(), Comment.TargetType.STUDY).size(),
                reportRepository.countByTargetTypeAndTargetId(Report.TargetType.POST, "study-" + study.getId())
        );
    }

    private int countPosts(Long userId) {
        return Math.toIntExact(freeBoardRepository.countByUserId(userId) + studyBoardRepository.countByUserId(userId));
    }

    private int countUserComments(Long userId) {
        return Math.toIntExact(commentRepository.countByUserId(userId));
    }

    private int countReportsForUser(Long userId) {
        return reportRepository.countByTargetTypeAndTargetId(Report.TargetType.USER, String.valueOf(userId));
    }

    private AdminUserResponse toAdminUserResponse(User user) {
        return AdminUserResponse.from(
                user,
                findSocialProvider(user.getId()),
                userPresenceService.isOnline(user.getId()),
                countPosts(user.getId()),
                countUserComments(user.getId()),
                countReportsForUser(user.getId())
        );
    }

    private String findSocialProvider(Long userId) {
        return oauth2AccountRepository.findAllByUserId(userId).stream()
                .findFirst()
                .map(OAuth2Account::getProvider)
                .map(Enum::name)
                .orElse("");
    }

    private User.Status parseUserStatus(String status) {
        return switch (normalizeStatus(status)) {
            case "active" -> User.Status.ACTIVE;
            case "suspended" -> User.Status.SUSPENDED;
            case "deleted" -> User.Status.DELETED;
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 사용자 상태입니다.");
        };
    }

    private FreeBoard.Status parseFreeStatus(String status) {
        return switch (normalizeStatus(status)) {
            case "visible" -> FreeBoard.Status.VISIBLE;
            case "hidden" -> FreeBoard.Status.HIDDEN;
            case "deleted" -> FreeBoard.Status.DELETED;
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 게시글 상태입니다.");
        };
    }

    private StudyBoard.Status parseStudyStatus(String status) {
        return switch (normalizeStatus(status)) {
            case "visible" -> StudyBoard.Status.VISIBLE;
            case "hidden" -> StudyBoard.Status.HIDDEN;
            case "deleted" -> StudyBoard.Status.DELETED;
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 게시글 상태입니다.");
        };
    }

    private Report.Status parseReportStatus(String status) {
        return switch (normalizeStatus(status)) {
            case "pending" -> Report.Status.PENDING;
            case "resolved" -> Report.Status.RESOLVED;
            case "dismissed" -> Report.Status.DISMISSED;
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 신고 상태입니다.");
        };
    }

    private AdminReportResponse toAdminReportResponse(Report report) {
        User reporter = report.getReporter();

        return new AdminReportResponse(
                String.valueOf(report.getId()),
                report.getTargetType().name().toLowerCase(Locale.ROOT),
                report.getTargetId(),
                report.getTargetLabel(),
                report.getReason(),
                new AdminReportResponse.ReporterInfo(
                        reporter == null ? "" : UserDisplayName.nickname(reporter),
                        reporter == null ? "" : safe(reporter.getEmail())
                ),
                report.getCreatedAt() == null ? "" : report.getCreatedAt().toString(),
                report.getStatus().name().toLowerCase(Locale.ROOT),
                report.getAdminResponse(),
                report.getRespondedAt() == null ? null : report.getRespondedAt().toString(),
                buildUserDetail(report),
                buildPostDetail(report),
                buildCommentDetail(report),
                buildChatDetail(report),
                buildInquiryDetail(report)
        );
    }

    private AdminReportResponse.UserDetail buildUserDetail(Report report) {
        if (report.getTargetType() != Report.TargetType.USER) return null;

        return userRepository.findById(parseLong(report.getTargetId(), "신고 대상 사용자 ID가 올바르지 않습니다."))
                .map(user -> new AdminReportResponse.UserDetail(
                        String.valueOf(user.getId()),
                        UserDisplayName.nickname(user),
                        safe(user.getEmail())
                ))
                .orElse(null);
    }

    private AdminReportResponse.PostDetail buildPostDetail(Report report) {
        if (report.getTargetType() != Report.TargetType.POST) return null;

        String[] parts = safe(report.getTargetId()).split("-", 2);
        if (parts.length != 2) return null;

        Long postId = parseLong(parts[1], "신고 대상 게시글 ID가 올바르지 않습니다.");
        if ("free".equalsIgnoreCase(parts[0])) {
            return freeBoardRepository.findById(postId)
                    .map(board -> new AdminReportResponse.PostDetail(
                            String.valueOf(board.getId()),
                            "free",
                            board.getTitle(),
                            safe(board.getContent()),
                            UserDisplayName.nickname(board.getUser())
                    ))
                    .orElse(null);
        }

        return studyBoardRepository.findById(postId)
                .map(study -> new AdminReportResponse.PostDetail(
                        String.valueOf(study.getId()),
                        "study",
                        study.getTitle(),
                        safe(study.getContent()),
                        UserDisplayName.nickname(study.getUser())
                ))
                .orElse(null);
    }

    private AdminReportResponse.CommentDetail buildCommentDetail(Report report) {
        if (report.getTargetType() != Report.TargetType.COMMENT) return null;

        return commentRepository.findById(parseLong(report.getTargetId(), "신고 대상 댓글 ID가 올바르지 않습니다."))
                .map(comment -> new AdminReportResponse.CommentDetail(
                        String.valueOf(comment.getId()),
                        safe(comment.getContent()),
                        UserDisplayName.nickname(comment.getUser()),
                        comment.getTargetType().name().toLowerCase(Locale.ROOT),
                        String.valueOf(comment.getTargetId()),
                        resolveCommentPostTitle(comment)
                ))
                .orElse(null);
    }

    private AdminReportResponse.ChatDetail buildChatDetail(Report report) {
        if (report.getTargetType() != Report.TargetType.CHAT) return null;

        Long roomId = parseLong(report.getTargetId(), "신고 대상 채팅방 ID가 올바르지 않습니다.");
        return chatRoomRepository.findById(roomId)
                .map(room -> {
                    User reporter = report.getReporter();
                    ChatParticipant partner = chatParticipantRepository.findByChatRoomId(roomId).stream()
                            .filter(participant -> reporter == null || !participant.getUser().getId().equals(reporter.getId()))
                            .findFirst()
                            .orElse(null);

                    AdminReportResponse.ChatPartner chatPartner = partner == null
                            ? null
                            : new AdminReportResponse.ChatPartner(
                                    String.valueOf(partner.getUser().getId()),
                                    UserDisplayName.nickname(partner.getUser()),
                                    safe(partner.getUser().getEmail())
                            );

                    List<AdminReportChatMessageResponse> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId).stream()
                            .filter(message -> message.getType() == ChatMessage.MessageType.TALK)
                            .sorted(Comparator.comparing(ChatMessage::getCreatedAt).reversed())
                            .limit(10)
                            .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                            .map(message -> new AdminReportChatMessageResponse(
                                    String.valueOf(message.getId()),
                                    message.getSender() == null ? "알 수 없음" : UserDisplayName.nickname(message.getSender()),
                                    safe(message.getMessage()),
                                    message.getCreatedAt() == null ? "" : message.getCreatedAt().toString()
                            ))
                            .toList();

                    return new AdminReportResponse.ChatDetail(
                            String.valueOf(room.getId()),
                            safe(room.getName()),
                            room.getType() == null ? "PERSONAL" : room.getType().name(),
                            chatPartner,
                            messages
                    );
                })
                .orElse(null);
    }

    private AdminReportResponse.InquiryDetail buildInquiryDetail(Report report) {
        if (report.getTargetType() != Report.TargetType.INQUIRY) return null;

        return new AdminReportResponse.InquiryDetail(
                report.getTargetLabel(),
                report.getReporter() == null ? "" : safe(report.getReporter().getEmail())
        );
    }

    private String resolveCommentPostTitle(Comment comment) {
        if (comment.getTargetType() == Comment.TargetType.FREE) {
            return freeBoardRepository.findById(comment.getTargetId())
                    .map(FreeBoard::getTitle)
                    .orElse("");
        }
        return studyBoardRepository.findById(comment.getTargetId())
                .map(StudyBoard::getTitle)
                .orElse("");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
    }

    private Long parseLong(String value, String message) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, message);
        }
    }
}
