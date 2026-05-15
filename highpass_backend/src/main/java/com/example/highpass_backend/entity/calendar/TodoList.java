package com.example.highpass_backend.entity.calendar;

import com.example.highpass_backend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String content;

    @Column(nullable = false)
    private LocalDate date;

    private boolean status = false;

    public void updateContent(String newContent) {
        if (newContent == null || newContent.trim().isEmpty()) {
            throw new RuntimeException("내용은 비어있을 수 없습니다");
        }
        this.content = newContent;
    }

}
