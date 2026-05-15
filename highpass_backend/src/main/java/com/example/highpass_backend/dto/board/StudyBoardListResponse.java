package com.example.highpass_backend.dto.board;

import com.example.highpass_backend.entity.board.StudyBoard;
import com.example.highpass_backend.dto.user.UserDisplayName;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StudyBoardListResponse {

    private Long id;
    private String title;
    private String content;
    private Long userId;
    private String nickname;
    private String avatarVisualClassName;
    private String locationName;
    private String address;
    private Double latitude;
    private Double longitude;
    private String cert;
    private int viewCount;
    private int likeCount;
    private boolean likedByUser;
    private LocalDateTime createdAt;

    public static StudyBoardListResponse from(StudyBoard study) {
        return from(study, false);
    }

    public static StudyBoardListResponse from(StudyBoard study, boolean likedByUser) {
        return StudyBoardListResponse.builder()
                .id(study.getId())
                .title(study.getTitle())
                .content(study.getContent())
                .userId(study.getUser().getId())
                .nickname(UserDisplayName.nickname(study.getUser()))
                .avatarVisualClassName(study.getUser().getAvatarVisualClassName())
                .locationName(study.getLocationName())
                .address(study.getAddress())
                .latitude(study.getLatitude())
                .longitude(study.getLongitude())
                .cert(study.getCert())
                .viewCount(study.getViewCount())
                .likeCount(study.getLikeCount())
                .likedByUser(likedByUser)
                .createdAt(study.getCreatedAt())
                .build();
    }
}
