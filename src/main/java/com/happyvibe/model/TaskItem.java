package com.happyvibe.model;

import java.time.LocalDateTime;

public record TaskItem(
        long id,
        long boardId,
        String title,
        String course,
        String description,
        LocalDateTime dueAt,
        TaskStatus status) {
}
