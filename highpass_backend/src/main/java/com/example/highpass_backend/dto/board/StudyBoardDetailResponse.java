package com.example.highpass_backend.dto.board;

import com.example.highpass_backend.entity.board.StudyBoard;
import com.example.highpass_backend.dto.user.UserDisplayName;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StudyBoardDetailResponse {

    private Long id;
    private String title;
    private String content;
    private Long userId;
    private String nickname;
    private String avatarVisualClassName;
    private String locationName;
    private String cert;
    private String address;
    private Double latitude;
    private Double longitude;
    private int viewCount;
    private int likeCount;
    private boolean likedByUser;
    private LocalDateTime createdAt;


    private Long chatRoomId;
    private long currentParticipants;
    private boolean isParticipant;
    private String participantStatus;

    public static StudyBoardDetailResponse from(StudyBoard study) {
        return from(study, false, null, 0, false, "NONE");
    }

    public static StudyBoardDetailResponse from(StudyBoard study, boolean likedByUser) {
        return from(study, likedByUser, null, 0, false, "NONE");
    }

    public static StudyBoardDetailResponse from(StudyBoard study, boolean likedByUser, Long chatRoomId, long currentParticipants, boolean isParticipant, String participantStatus) {
        return StudyBoardDetailResponse.builder()
                .id(study.getId())
                .title(study.getTitle())
                .content(study.getContent())
                .userId(study.getUser().getId())
                .nickname(UserDisplayName.nickname(study.getUser()))
                .avatarVisualClassName(study.getUser().getAvatarVisualClassName())
                .locationName(study.getLocationName())
                .cert(study.getCert())
                .address(study.getAddress())
                .latitude(study.getLatitude())
                .longitude(study.getLongitude())
                .viewCount(study.getViewCount())
                .likeCount(study.getLikeCount())
                .likedByUser(likedByUser)
                .createdAt(study.getCreatedAt())
                .chatRoomId(chatRoomId)
                .currentParticipants(currentParticipants)
                .isParticipant(isParticipant)
                .participantStatus(participantStatus)
                .build();
    }
}
