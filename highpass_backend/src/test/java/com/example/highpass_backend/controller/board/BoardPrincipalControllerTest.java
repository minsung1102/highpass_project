package com.example.highpass_backend.controller.board;

import com.example.highpass_backend.dto.board.FreeBoardRequest;
import com.example.highpass_backend.dto.board.FreeBoardResponse;
import com.example.highpass_backend.entity.board.BoardLike;
import com.example.highpass_backend.security.CustomJwtPrincipal;
import com.example.highpass_backend.service.board.BoardLikeService;
import com.example.highpass_backend.service.board.FreeBoardService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BoardPrincipalControllerTest {

    @Test
    void createFreeBoardUsesAuthenticatedPrincipalUserId() {
        FreeBoardService freeBoardService = mock(FreeBoardService.class);
        FreeBoardController controller = new FreeBoardController(freeBoardService);
        CustomJwtPrincipal principal = new CustomJwtPrincipal(7L, "writer@example.com");
        FreeBoardRequest request = new FreeBoardRequest("title", "content");
        FreeBoardResponse response = FreeBoardResponse.builder()
                .id(10L)
                .userId(7L)
                .title("title")
                .content("content")
                .build();

        when(freeBoardService.createFreeBoard(7L, request)).thenReturn(response);

        var result = controller.addFreeBoard(principal, request);

        assertThat(result.getStatusCode().value()).isEqualTo(201);
        assertThat(result.getBody()).isSameAs(response);
        verify(freeBoardService).createFreeBoard(7L, request);
    }

    @Test
    void updateAndDeleteFreeBoardUseAuthenticatedPrincipalUserId() {
        FreeBoardService freeBoardService = mock(FreeBoardService.class);
        FreeBoardController controller = new FreeBoardController(freeBoardService);
        CustomJwtPrincipal principal = new CustomJwtPrincipal(9L, "writer@example.com");
        FreeBoardRequest request = new FreeBoardRequest("updated", "updated content");
        FreeBoardResponse response = FreeBoardResponse.builder()
                .id(15L)
                .userId(9L)
                .title("updated")
                .content("updated content")
                .build();

        when(freeBoardService.updateFreeBoard(9L, 15L, request)).thenReturn(response);

        var updateResult = controller.updateBoard(15L, principal, request);
        var deleteResult = controller.deleteFreeBoard(15L, principal);

        assertThat(updateResult.getBody()).isSameAs(response);
        assertThat(deleteResult.getStatusCode().is2xxSuccessful()).isTrue();
        verify(freeBoardService).updateFreeBoard(9L, 15L, request);
        verify(freeBoardService).deleteFreeBoard(9L, 15L);
    }

    @Test
    void toggleLikeUsesAuthenticatedPrincipalUserId() {
        BoardLikeService boardLikeService = mock(BoardLikeService.class);
        BoardLikeController controller = new BoardLikeController(boardLikeService);
        CustomJwtPrincipal principal = new CustomJwtPrincipal(11L, "reader@example.com");

        var result = controller.toggleLike(principal, 20L, BoardLike.TargetType.FREE);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        verify(boardLikeService).toggleLike(11L, BoardLike.TargetType.FREE, 20L);
    }
}
