package com.example.highpass_backend.controller.calendar;

import com.example.highpass_backend.dto.calendar.TodoListRequest;
import com.example.highpass_backend.dto.calendar.TodoListResponse;
import com.example.highpass_backend.dto.calendar.TodoUpdateContentRequest;
import com.example.highpass_backend.security.CustomJwtPrincipal;
import com.example.highpass_backend.service.calendar.TodoListService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoListController {
    private final TodoListService todoListService;

    // 할 일 등록
    @PostMapping
    public ResponseEntity<TodoListResponse> createTodoList(
            @AuthenticationPrincipal CustomJwtPrincipal principal,
            @Valid @RequestBody TodoListRequest request
    ) {
        TodoListResponse todoListResponse = todoListService.createTodo(principal.getUserId(), request);
        return new ResponseEntity<>(todoListResponse,HttpStatus.CREATED);
    }

    // 목록 조회
    @GetMapping
    public ResponseEntity<List<TodoListResponse>> getList(
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        List<TodoListResponse> responses = todoListService.getTodosByDate(principal.getUserId());
        return ResponseEntity.ok(responses);
    }

    // 완료 상태 변경
    @PatchMapping("/{todoId}/status")
    public ResponseEntity<TodoListResponse> done(
            @PathVariable("todoId") Long todoId,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        TodoListResponse updateTodo = todoListService.toggleStatus(principal.getUserId(), todoId);
        return ResponseEntity.ok(updateTodo);
    }

    // 내용 수정
    @PatchMapping("/{todoId}/content")
    public ResponseEntity<TodoListResponse> updateContent(
            @PathVariable("todoId") Long todoId,
            @AuthenticationPrincipal CustomJwtPrincipal principal,
            @Valid @RequestBody TodoUpdateContentRequest request
    ) {
        TodoListResponse updatedTodo = todoListService.updateContent(principal.getUserId(), todoId, request.getContent());
        return ResponseEntity.ok(updatedTodo);
    }

    // 할 일 삭제
    @DeleteMapping("/{todoId}")
    public ResponseEntity<Void> delete(
            @PathVariable("todoId") Long todoId,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        todoListService.deleteTodo(principal.getUserId(), todoId);
        return ResponseEntity.ok().build();
    }
}
