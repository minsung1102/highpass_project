package com.example.highpass_backend.service.board;

import com.example.highpass_backend.dto.board.FreeBoardRequest;
import com.example.highpass_backend.dto.board.FreeBoardResponse;
import com.example.highpass_backend.eception.BusinessException;
import com.example.highpass_backend.eception.ErrorCode;
import com.example.highpass_backend.entity.board.BoardLike;
import com.example.highpass_backend.entity.board.Comment;
import com.example.highpass_backend.entity.board.FreeBoard;
import com.example.highpass_backend.entity.user.User;
import com.example.highpass_backend.repository.board.FreeBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FreeBoardService {
    private final FreeBoardRepository freeBoardRepository;
    private final BoardSupportService boardSupportService;

    @Transactional
    public FreeBoardResponse createFreeBoard(Long userId, FreeBoardRequest request) {
        User user = boardSupportService.getAuthenticatedUser(userId);

        FreeBoard freeBoard = FreeBoard.builder()
                .user(user)
                .title(request.title())
                .content(request.content())
                .tags(request.tags() != null ? String.join(",", request.tags()) : null)
                .build();

        return FreeBoardResponse.from(freeBoardRepository.save(freeBoard));
    }

    @Transactional(readOnly = true)
    public List<FreeBoardResponse> getFreeBoardList(Long currentUserId) {
        return freeBoardRepository.findByStatusOrStatusIsNullOrderByCreatedAtDesc(FreeBoard.Status.VISIBLE).stream()
                .map(board -> FreeBoardResponse.from(
                        board,
                        boardSupportService.isLikedByUser(currentUserId, BoardLike.TargetType.FREE, board.getId())
                ))
                .toList();
    }

    @Transactional
    public FreeBoardResponse getFreeBoard(Long freeBoardId, Long currentUserId) {
        FreeBoard freeBoard = getVisibleBoard(freeBoardId);
        freeBoard.increaseViewCount();
        return FreeBoardResponse.from(
                freeBoard,
                boardSupportService.isLikedByUser(currentUserId, BoardLike.TargetType.FREE, freeBoard.getId())
        );
    }

    @Transactional
    public void deleteFreeBoard(Long currentUserId, Long freeBoardId) {
        FreeBoard freeBoard = getBoard(freeBoardId);
        boardSupportService.assertCanModify(currentUserId, freeBoard.getUser().getId());

        boardSupportService.deleteBoardInteractions(Comment.TargetType.FREE, BoardLike.TargetType.FREE, freeBoardId);
        freeBoardRepository.delete(freeBoard);
    }

    @Transactional
    public FreeBoardResponse updateFreeBoard(Long currentUserId, Long freeBoardId, FreeBoardRequest request) {
        FreeBoard freeBoard = getBoard(freeBoardId);
        boardSupportService.assertCanModify(currentUserId, freeBoard.getUser().getId());

        freeBoard.updateBoard(request.title(), request.content());
        freeBoard.setTags(request.tags() != null ? String.join(",", request.tags()) : null);
        return FreeBoardResponse.from(freeBoard);
    }

    private FreeBoard getVisibleBoard(Long freeBoardId) {
        FreeBoard freeBoard = getBoard(freeBoardId);
        if (!isVisible(freeBoard)) {
            throw notFound();
        }
        return freeBoard;
    }

    private FreeBoard getBoard(Long freeBoardId) {
        return freeBoardRepository.findById(freeBoardId)
                .orElseThrow(this::notFound);
    }

    private boolean isVisible(FreeBoard freeBoard) {
        return freeBoard.getStatus() == null || freeBoard.getStatus() == FreeBoard.Status.VISIBLE;
    }

    private BusinessException notFound() {
        return new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 게시글입니다.");
    }
}
