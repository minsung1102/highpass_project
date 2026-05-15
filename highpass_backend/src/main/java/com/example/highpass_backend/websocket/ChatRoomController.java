package com.example.highpass_backend.websocket;

import com.example.highpass_backend.dto.chat.ChatParticipantResponse;
import com.example.highpass_backend.dto.chat.ChatReadStateRequest;
import com.example.highpass_backend.dto.chat.ChatRoomReadStateResponse;
import com.example.highpass_backend.dto.chat.ChatRoomResponse;
import com.example.highpass_backend.dto.chat.GroupChatCreateRequest;
import com.example.highpass_backend.dto.chat.StudyChatJoinResponse;
import com.example.highpass_backend.entity.chat.ChatRoom;
import com.example.highpass_backend.repository.chat.ChatRoomRepository;
import com.example.highpass_backend.security.CustomJwtPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatRoomController {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatService chatService;

    @GetMapping("/rooms")
    public List<ChatRoomResponse> getRooms(@AuthenticationPrincipal CustomJwtPrincipal principal) {
        if (principal == null) {
            return Collections.emptyList();
        }
        return chatService.getMyRooms(principal.getUserId());
    }

    @PostMapping("/room")
    public ChatRoomResponse createRoom(
            @AuthenticationPrincipal CustomJwtPrincipal principal,
            @RequestParam(name = "partnerId") Long partnerId
    ) {
        ChatRoom chatRoom = chatService.createOneToOneRoom(principal.getUserId(), partnerId);
        return new ChatRoomResponse(chatRoom, principal.getUserId());
    }

    @GetMapping("/room/{roomId}")
    public ChatRoomResponse getRoomInfo(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));
        return new ChatRoomResponse(chatRoom, principal.getUserId());
    }

    @PostMapping("/rooms/{roomId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        chatService.updateLastReadTime(roomId, principal.getUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rooms/{roomId}/read-state")
    public ResponseEntity<ChatRoomReadStateResponse> getReadState(
            @PathVariable Long roomId,
            @RequestBody(required = false) ChatReadStateRequest request,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        List<Long> messageIds = request != null ? request.messageIds() : Collections.emptyList();
        return ResponseEntity.ok(chatService.getReadState(roomId, principal.getUserId(), messageIds));
    }

    @GetMapping("/rooms/{roomId}/pending")
    public ResponseEntity<List<ChatParticipantResponse>> getPendingParticipants(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        return ResponseEntity.ok(chatService.getPendingParticipants(roomId, principal.getUserId()));
    }

    @PostMapping("/rooms/{roomId}/approve/{targetUserId}")
    public ResponseEntity<Void> approveParticipant(
            @PathVariable Long roomId,
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        chatService.approveParticipant(roomId, principal.getUserId(), targetUserId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/rooms/{roomId}/reject/{targetUserId}")
    public ResponseEntity<Void> rejectParticipant(
            @PathVariable Long roomId,
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        chatService.rejectParticipant(roomId, principal.getUserId(), targetUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/room/group")
    public ResponseEntity<ChatRoomResponse> createGroupRoom(
            @RequestBody GroupChatCreateRequest request,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        ChatRoom groupChatRoom = chatService.createGroupChatRoom(
                request.getName(),
                principal.getUserId(),
                Boolean.TRUE.equals(request.getApprovalRequired())
        );
        return ResponseEntity.ok(new ChatRoomResponse(groupChatRoom, principal.getUserId()));
    }

    @DeleteMapping("/rooms/{roomId}/kick/{targetUserId}")
    public ResponseEntity<Void> kickParticipant(
            @PathVariable Long roomId,
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        chatService.kickParticipant(roomId, principal.getUserId(), targetUserId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/rooms/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        chatService.leaveRoom(roomId, principal.getUserId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/rooms/{roomId}/join-request")
    public ResponseEntity<Void> cancelJoinRequest(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        chatService.cancelJoinRequest(roomId, principal.getUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/room/{studyId}/join")
    public ResponseEntity<StudyChatJoinResponse> joinStudy(
            @PathVariable Long studyId,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        return ResponseEntity.ok(chatService.joinStudyChat(studyId, principal.getUserId()));
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        chatService.deleteMessage(messageId, principal.getUserId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/rooms/{roomId}/owner/{targetUserId}")
    public ResponseEntity<Void> transferOwner(
            @PathVariable Long roomId,
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        chatService.transferOwner(roomId, principal.getUserId(), targetUserId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/rooms/{roomId}/nickname")
    public ResponseEntity<Void> updateChatRoomName(
            @PathVariable Long roomId,
            @RequestParam String newNickname,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        chatService.updateChatRoomName(roomId, principal.getUserId(), newNickname);
        return ResponseEntity.ok().build();
    }
}
