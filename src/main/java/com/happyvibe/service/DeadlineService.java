package com.happyvibe.service;

import com.happyvibe.model.DeadlineStatus;
import com.happyvibe.model.TaskItem;
import com.happyvibe.model.TaskStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class DeadlineService {
    private final Clock clock;

    public DeadlineService(Clock clock) {
        this.clock = clock;
    }

    public DeadlineStatus classify(TaskItem task) {
        if (task.status() == TaskStatus.DONE) {
            return DeadlineStatus.COMPLETED;
        }
        if (task.dueAt() == null) {
            return DeadlineStatus.NO_DEADLINE;
        }
        LocalDateTime now = LocalDateTime.now(clock);
        if (task.dueAt().isBefore(now)) {
            return DeadlineStatus.OVERDUE;
        }
        if (!task.dueAt().isAfter(now.plusHours(48))) {
            return DeadlineStatus.DUE_SOON;
        }
        return DeadlineStatus.ON_TIME;
    }
}
