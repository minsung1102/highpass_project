package com.example.highpass_backend.service.report;

import com.example.highpass_backend.dto.report.CreateReportRequest;
import com.example.highpass_backend.dto.report.CreateSupportInquiryRequest;
import com.example.highpass_backend.dto.report.ReportResponse;
import com.example.highpass_backend.dto.user.UserDisplayName;
import com.example.highpass_backend.eception.BusinessException;
import com.example.highpass_backend.eception.ErrorCode;
import com.example.highpass_backend.entity.board.Comment;
import com.example.highpass_backend.entity.board.FreeBoard;
import com.example.highpass_backend.entity.board.StudyBoard;
import com.example.highpass_backend.entity.chat.ChatParticipant;
import com.example.highpass_backend.entity.chat.ChatRoom;
import com.example.highpass_backend.entity.report.Report;
import com.example.highpass_backend.entity.user.User;
import com.example.highpass_backend.repository.board.CommentRepository;
import com.example.highpass_backend.repository.board.FreeBoardRepository;
import com.example.highpass_backend.repository.board.StudyBoardRepository;
import com.example.highpass_backend.repository.chat.ChatParticipantRepository;
import com.example.highpass_backend.repository.chat.ChatRoomRepository;
import com.example.highpass_backend.repository.report.ReportRepository;
import com.example.highpass_backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final FreeBoardRepository freeBoardRepository;
    private final StudyBoardRepository studyBoardRepository;
    private final CommentRepository commentRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    @Transactional(readOnly = true)
    public List<ReportResponse> getMyReports(Long userId) {
        return reportRepository.findByReporterIdOrderByCreatedAtDesc(userId).stream()
                .map(ReportResponse::from)
                .toList();
    }

    @Transactional
    public ReportResponse createReport(Long reporterUserId, CreateReportRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "신고 요청 본문이 필요합니다.");
        }

        User reporter = userRepository.findById(reporterUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "신고자를 찾을 수 없습니다."));

        Report.TargetType targetType = parseTargetType(request.targetType());
        String reasonCode = normalizeRequired(request.reasonCode(), "신고 사유");
        String detail = normalizeRequired(request.detail(), "신고 상세 내용");
        if (detail.length() < 10) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "신고 상세 내용은 10자 이상이어야 합니다.");
        }

        ResolvedTarget target = resolveTarget(reporter, targetType, request.targetId(), request.targetLabel());

        Report report = reportRepository.save(
                Report.builder()
                        .reporter(reporter)
                        .targetType(targetType)
                        .targetId(target.targetId())
                        .targetLabel(target.targetLabel())
                        .reasonCode(reasonCode)
                        .reason(detail)
                        .build()
        );

        return ReportResponse.from(report);
    }

    @Transactional
    public ReportResponse createSupportInquiry(CreateSupportInquiryRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "문의 요청 본문이 필요합니다.");
        }

        String email = normalizeRequired(request.email(), "이메일");
        String title = normalizeRequired(request.title(), "문의 제목");
        String reasonCode = normalizeRequired(request.reasonCode(), "문의 사유");
        String detail = normalizeRequired(request.detail(), "문의 상세 내용");
        if (detail.length() < 10) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "문의 상세 내용은 10자 이상이어야 합니다.");
        }

        User reporter = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "가입된 이메일로만 계정 문의를 접수할 수 있습니다."));

        Report report = reportRepository.save(
                Report.builder()
                        .reporter(reporter)
                        .targetType(Report.TargetType.INQUIRY)
                        .targetId("support-" + reporter.getId())
                        .targetLabel(title)
                        .reasonCode(reasonCode)
                        .reason(detail)
                        .build()
        );

        return ReportResponse.from(report);
    }

    private Report.TargetType parseTargetType(String targetType) {
        String normalized = normalizeRequired(targetType, "신고 대상 유형");
        try {
            return Report.TargetType.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 신고 대상 유형입니다.");
        }
    }

    private ResolvedTarget resolveTarget(User reporter, Report.TargetType targetType, String rawTargetId, String rawTargetLabel) {
        return switch (targetType) {
            case USER -> resolveUserTarget(reporter, rawTargetId);
            case POST -> resolvePostTarget(reporter, rawTargetId);
            case COMMENT -> resolveCommentTarget(reporter, rawTargetId);
            case CHAT -> resolveChatTarget(reporter, rawTargetId);
            case INQUIRY -> resolveInquiryTarget(reporter, rawTargetId, rawTargetLabel);
        };
    }

    private ResolvedTarget resolveUserTarget(User reporter, String rawTargetId) {
        Long targetUserId = parseNumericTargetId(rawTargetId, "신고 대상 사용자 ID가 올바르지 않습니다.");
        if (reporter.getId().equals(targetUserId)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "본인은 신고할 수 없습니다.");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고 대상을 찾을 수 없습니다."));

        return new ResolvedTarget(String.valueOf(targetUser.getId()), UserDisplayName.nickname(targetUser));
    }

    private ResolvedTarget resolvePostTarget(User reporter, String rawTargetId) {
        String normalized = normalizeRequired(rawTargetId, "신고 대상 ID");
        String[] parts = normalized.split("-", 2);
        if (parts.length != 2) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "게시글 신고 대상 ID는 게시판 유형을 포함해야 합니다.");
        }

        Long postId = parseNumericTargetId(parts[1], "게시글 ID가 올바르지 않습니다.");
        return switch (parts[0].toLowerCase(Locale.ROOT)) {
            case "free" -> {
                FreeBoard board = freeBoardRepository.findById(postId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고 대상 게시글을 찾을 수 없습니다."));
                if (reporter.getId().equals(board.getUser().getId())) {
                    throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "본인 게시글은 신고할 수 없습니다.");
                }
                yield new ResolvedTarget("free-" + board.getId(), "[자유] " + board.getTitle());
            }
            case "study" -> {
                StudyBoard study = studyBoardRepository.findById(postId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고 대상 게시글을 찾을 수 없습니다."));
                if (reporter.getId().equals(study.getUser().getId())) {
                    throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "본인 게시글은 신고할 수 없습니다.");
                }
                yield new ResolvedTarget("study-" + study.getId(), "[스터디] " + study.getTitle());
            }
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 게시판 유형입니다.");
        };
    }

    private ResolvedTarget resolveCommentTarget(User reporter, String rawTargetId) {
        Long commentId = parseNumericTargetId(rawTargetId, "댓글 ID가 올바르지 않습니다.");
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고 대상 댓글을 찾을 수 없습니다."));
        if (reporter.getId().equals(comment.getUser().getId())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "본인 댓글은 신고할 수 없습니다.");
        }

        String label = UserDisplayName.nickname(comment.getUser()) + " - " + abbreviate(comment.getContent(), 36);
        return new ResolvedTarget(String.valueOf(comment.getId()), label);
    }

    private ResolvedTarget resolveChatTarget(User reporter, String rawTargetId) {
        Long roomId = parseNumericTargetId(rawTargetId, "채팅방 ID가 올바르지 않습니다.");
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고 대상 채팅방을 찾을 수 없습니다."));
        if (!chatParticipantRepository.existsByChatRoomIdAndUserId(roomId, reporter.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "참여 중인 채팅방만 신고할 수 있습니다.");
        }

        ChatParticipant partner = chatParticipantRepository.findByChatRoomId(roomId).stream()
                .filter(participant -> !participant.getUser().getId().equals(reporter.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신고 대상 채팅 상대를 찾을 수 없습니다."));

        String roomLabel = room.getName();
        if (roomLabel == null || roomLabel.isBlank()) {
            roomLabel = UserDisplayName.nickname(partner.getUser());
        }
        return new ResolvedTarget(String.valueOf(room.getId()), roomLabel);
    }

    private ResolvedTarget resolveInquiryTarget(User reporter, String rawTargetId, String rawTargetLabel) {
        String targetId = rawTargetId == null || rawTargetId.trim().isBlank()
                ? "support-" + reporter.getId()
                : rawTargetId.trim();
        String targetLabel = normalizeRequired(rawTargetLabel, "문의 제목");
        return new ResolvedTarget(targetId, targetLabel);
    }

    private Long parseNumericTargetId(String targetId, String errorMessage) {
        String normalized = normalizeRequired(targetId, "신고 대상 ID");
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException exception) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, errorMessage);
        }
    }

    private String normalizeRequired(String value, String fieldName) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, fieldName + "은(는) 필수입니다.");
        }
        return normalized;
    }

    private String abbreviate(String value, int maxLength) {
        String normalized = normalizeRequired(value, "라벨");
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, Math.max(0, maxLength - 1)) + "...";
    }

    private record ResolvedTarget(String targetId, String targetLabel) {
    }
}
