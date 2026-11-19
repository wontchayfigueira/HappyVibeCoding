
package com.happyvibe.service;

import com.happyvibe.model.TaskItem;
import com.happyvibe.model.TaskStatus;
import com.happyvibe.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository tasks;
    private final BoardService boardService;
    private final AppSession session;

    public TaskService(TaskRepository tasks,
                       BoardService boardService,
                       AppSession session) {
        this.tasks = tasks;
        this.boardService = boardService;
        this.session = session;
    }

    public long createTask(long boardId, String title,
                           String course, String description,
                           LocalDateTime dueAt) {

        boardService.requireMember(boardId);

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Task title cannot be empty");
        }

        return tasks.create(
                boardId,
                title.trim(),
                course,
                description,
                dueAt
        );
    }

    public List<TaskItem> findByBoard(long boardId) {
        boardService.requireMember(boardId);
        return tasks.findByBoard(boardId);
    }

    public List<TaskItem> findMyTasks() {
        long userId = session.currentUser().id();
        return tasks.findAssignedToUser(userId);
    }

    public TaskItem findById(long taskId) {
        TaskItem task = requireTask(taskId);
        return task;
    }

    public void updateTask(long taskId, String title,
                           String course, String description,
                           LocalDateTime dueAt) {

        TaskItem task = requireTask(taskId);

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Task title cannot be empty");
        }

        tasks.update(
                task.id(),
                title.trim(),
                course,
                description,
                dueAt
        );
    }

    public void changeStatus(long taskId, TaskStatus status) {

        TaskItem task = requireTask(taskId);

        if (status == null) {
            throw new IllegalArgumentException(
                    "Task status cannot be null");
        }

        tasks.updateStatus(task.id(), status);
    }

    public void deleteTask(long taskId) {
        TaskItem task = requireTask(taskId);
        tasks.delete(task.id());
    }

    private TaskItem requireTask(long taskId) {

        TaskItem task = tasks.findById(taskId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Task not found: " + taskId));

        boardService.requireMember(task.boardId());

        return task;
    }
}
