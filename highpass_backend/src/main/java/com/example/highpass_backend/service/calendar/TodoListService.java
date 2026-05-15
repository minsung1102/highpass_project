package com.example.highpass_backend.service.calendar;

import com.example.highpass_backend.dto.calendar.TodoListRequest;
import com.example.highpass_backend.dto.calendar.TodoListResponse;
import com.example.highpass_backend.eception.BusinessException;
import com.example.highpass_backend.eception.ErrorCode;
import com.example.highpass_backend.entity.calendar.TodoList;
import com.example.highpass_backend.entity.user.User;
import com.example.highpass_backend.repository.calendar.TodoListRepository;
import com.example.highpass_backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoListService {
    private final TodoListRepository todolistRepository;
    private final UserRepository userRepository;

    @Transactional
    public TodoListResponse createTodo(Long userId, TodoListRequest request) {
        User user = getUser(userId);

        TodoList todoList = TodoList.builder()
                .user(user)
                .content(request.getContent())
                .date(request.getDate())
                .status(Boolean.TRUE.equals(request.getStatus()))
                .build();

        return TodoListResponse.from(todolistRepository.save(todoList));
    }

    @Transactional(readOnly = true)
    public List<TodoListResponse> getTodosByDate(Long userId) {
        User user = getUser(userId);

        return todolistRepository.findAllByUser(user)
                .stream()
                .map(TodoListResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public TodoListResponse toggleStatus(Long currentUserId, Long id) {
        TodoList todo = getTodo(id);
        assertOwner(currentUserId, todo);

        todo.setStatus(!todo.isStatus());
        return TodoListResponse.from(todo);
    }

    @Transactional
    public TodoListResponse updateContent(Long currentUserId, Long todoId, String newContent) {
        TodoList todoList = getTodo(todoId);
        assertOwner(currentUserId, todoList);
        todoList.updateContent(newContent);
        return TodoListResponse.from(todolistRepository.save(todoList));
    }

    @Transactional
    public void deleteTodo(Long currentUserId, Long id) {
        TodoList todoList = getTodo(id);
        assertOwner(currentUserId, todoList);
        todolistRepository.delete(todoList);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "인증된 사용자를 찾을 수 없습니다."));
    }

    private TodoList getTodo(Long todoId) {
        return todolistRepository.findById(todoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 할 일입니다."));
    }

    private void assertOwner(Long currentUserId, TodoList todoList) {
        if (!todoList.getUser().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "할 일에 접근할 권한이 없습니다.");
        }
    }
}
