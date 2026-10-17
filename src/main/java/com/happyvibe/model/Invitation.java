package com.happyvibe.model;

import java.time.LocalDateTime;

public record Invitation(
        long id,
        long boardId,
        String boardName,
        String inviterUsername,
        String status,
        LocalDateTime createdAt) {
}
