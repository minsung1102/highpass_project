package com.example.highpass_backend.service.board;

import com.example.highpass_backend.dto.board.StudyBoardCreateRequest;
import com.example.highpass_backend.dto.board.StudyBoardDetailResponse;
import com.example.highpass_backend.dto.board.StudyBoardListResponse;
import com.example.highpass_backend.eception.BusinessException;
import com.example.highpass_backend.eception.ErrorCode;
import com.example.highpass_backend.entity.board.BoardLike;
import com.example.highpass_backend.entity.board.Comment;
import com.example.highpass_backend.entity.board.StudyBoard;
import com.example.highpass_backend.entity.chat.ChatParticipant;
import com.example.highpass_backend.entity.chat.ChatRoom;
import com.example.highpass_backend.entity.user.User;
import com.example.highpass_backend.repository.board.StudyBoardRepository;
import com.example.highpass_backend.repository.chat.ChatParticipantRepository;
import com.example.highpass_backend.repository.chat.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudyBoardService {
    private final StudyBoardRepository studyBoardRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final BoardSupportService boardSupportService;

    @Transactional
    public StudyBoardDetailResponse createStudy(Long userId, StudyBoardCreateRequest request) {
        User user = boardSupportService.getAuthenticatedUser(userId);

        StudyBoard study = StudyBoard.builder()
                .user(user)
                .title(request.title())
                .content(request.content())
                .locationName(request.locationName())
                .cert(request.cert())
                .address(request.address())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .placeId(request.placeId())
                .build();
        StudyBoard savedStudy = studyBoardRepository.save(study);

        ChatRoom savedChatRoom = null;
        if (request.createChatRoom()) {
            ChatRoom chatRoom = ChatRoom.builder()
                    .name(request.title())
                    .ownerId(userId)
                    .isApprovalRequired(true)
                    .type(ChatRoom.ChatType.GROUP)
                    .build();
            savedChatRoom = chatRoomRepository.save(chatRoom);
            ChatParticipant ownerParticipant = chatRoom.addParticipant(user, true);
            chatParticipantRepository.save(ownerParticipant);
            savedStudy.setChatRoom(savedChatRoom);
        }

        return StudyBoardDetailResponse.from(
                savedStudy,
                false,
                savedChatRoom != null ? savedChatRoom.getId() : null,
                0,
                true,
                "JOINED"
        );
    }

    @Transactional(readOnly = true)
    public List<StudyBoardListResponse> getStudyList(Long currentUserId) {
        return studyBoardRepository.findByStatusOrStatusIsNullOrderByCreatedAtDesc(StudyBoard.Status.VISIBLE).stream()
                .map(study -> StudyBoardListResponse.from(
                        study,
                        boardSupportService.isLikedByUser(currentUserId, BoardLike.TargetType.STUDY, study.getId())
                ))
                .toList();
    }

    @Transactional
    public StudyBoardDetailResponse getStudy(Long studyId, Long currentUserId) {
        StudyBoard study = getVisibleStudy(studyId);
        study.incrementViewCount();

        ChatRoom room = study.getChatRoom();
        Long chatRoomId = null;
        long currentParticipants = 0;
        boolean isParticipant = false;
        String participantStatus = "NONE";

        if (room != null) {
            chatRoomId = room.getId();
            currentParticipants = room.getParticipants().stream()
                    .filter(p -> getParticipantStatus(p) == ChatParticipant.ParticipantStatus.JOINED)
                    .count();

            if (currentUserId != null) {
                Optional<ChatParticipant> participantOpt = room.getParticipants().stream()
                        .filter(p -> p.getUser().getId().equals(currentUserId))
                        .findFirst();

                if (participantOpt.isPresent()) {
                    ChatParticipant participant = participantOpt.get();
                    ChatParticipant.ParticipantStatus status = getParticipantStatus(participant);
                    participantStatus = status.name();
                    isParticipant = status == ChatParticipant.ParticipantStatus.JOINED;
                }
            }
        }

        return StudyBoardDetailResponse.from(
                study,
                boardSupportService.isLikedByUser(currentUserId, BoardLike.TargetType.STUDY, study.getId()),
                chatRoomId,
                currentParticipants,
                isParticipant,
                participantStatus
        );
    }

    @Transactional
    public void deleteStudy(Long currentUserId, Long studyId) {
        StudyBoard study = getStudyBoard(studyId);
        boardSupportService.assertCanModify(currentUserId, study.getUser().getId());

        boardSupportService.deleteBoardInteractions(Comment.TargetType.STUDY, BoardLike.TargetType.STUDY, studyId);
        studyBoardRepository.delete(study);
    }

    @Transactional
    public StudyBoardDetailResponse updateStudy(Long currentUserId, Long studyId, StudyBoardCreateRequest request) {
        StudyBoard study = getStudyBoard(studyId);
        boardSupportService.assertCanModify(currentUserId, study.getUser().getId());

        study.updateStudy(
                request.title(),
                request.content(),
                request.locationName(),
                request.address(),
                request.latitude(),
                request.longitude(),
                request.placeId(),
                request.cert()
        );

        return StudyBoardDetailResponse.from(study);
    }

    private StudyBoard getVisibleStudy(Long studyId) {
        StudyBoard study = getStudyBoard(studyId);
        if (!isVisible(study)) {
            throw notFound();
        }
        return study;
    }

    private StudyBoard getStudyBoard(Long studyId) {
        return studyBoardRepository.findById(studyId)
                .orElseThrow(this::notFound);
    }

    private boolean isVisible(StudyBoard study) {
        return study.getStatus() == null || study.getStatus() == StudyBoard.Status.VISIBLE;
    }

    private ChatParticipant.ParticipantStatus getParticipantStatus(ChatParticipant participant) {
        return participant.getStatus() != null
                ? participant.getStatus()
                : ChatParticipant.ParticipantStatus.JOINED;
    }

    private BusinessException notFound() {
        return new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 스터디 게시글입니다.");
    }
}
