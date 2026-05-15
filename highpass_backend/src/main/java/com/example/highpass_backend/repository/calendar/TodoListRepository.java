package com.example.highpass_backend.repository.calendar;

import com.example.highpass_backend.entity.calendar.TodoList;
import com.example.highpass_backend.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoListRepository extends JpaRepository<TodoList, Long> {

    List<TodoList> findAllByUser(User user);  //  모든 내용 조회
}
