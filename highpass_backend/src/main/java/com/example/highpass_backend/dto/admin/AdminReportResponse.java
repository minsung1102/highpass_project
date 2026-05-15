package com.example.highpass_backend.dto.admin;

import java.util.List;

public record AdminReportResponse(
        String id,
        String targetType,
        String targetId,
        String targetLabel,
        String reason,
        ReporterInfo reporter,
        String createdAt,
        String status,
        String adminResponse,
        String respondedAt,
        UserDetail userDetail,
        PostDetail postDetail,
        CommentDetail commentDetail,
        ChatDetail chatDetail,
        InquiryDetail inquiryDetail
) {
    public record ReporterInfo(
            String name,
            String email
    ) {
    }

    public record UserDetail(
            String userId,
            String nickname,
            String email
    ) {
    }

    public record PostDetail(
            String postId,
            String postType,
            String title,
            String content,
            String author
    ) {
    }

    public record CommentDetail(
            String commentId,
            String content,
            String author,
            String postType,
            String postId,
            String postTitle
    ) {
    }

    public record ChatDetail(
            String roomId,
            String roomName,
            String roomType,
            ChatPartner partner,
            List<AdminReportChatMessageResponse> messages
    ) {
    }

    public record ChatPartner(
            String userId,
            String nickname,
            String email
    ) {
    }

    public record InquiryDetail(
            String title,
            String accountEmail
    ) {
    }
}
