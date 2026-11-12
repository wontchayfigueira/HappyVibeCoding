package com.happyvibe.repository;

import com.happyvibe.model.TaskItem;
import com.happyvibe.model.TaskStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class TaskRepository {
    private final JdbcTemplate jdbc;

    public TaskRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long create(long boardId, String title, String course, String description, LocalDateTime dueAt) {
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO tasks(board_id, title, course, description, due_at, status)
                    VALUES (?, ?, ?, ?, ?, 'TODO')
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, boardId);
            ps.setString(2, title);
            ps.setString(3, course);
            ps.setString(4, description);
            ps.setString(5, dueAt == null ? null : dueAt.toString());
            return ps;
        }, key);
        if (key.getKey() == null) {
            throw new IllegalStateException("Could not create task");
        }
        return key.getKey().longValue();
    }

    public Optional<TaskItem> findById(long taskId) {
        List<TaskItem> result = jdbc.query(
                "SELECT id, board_id, title, course, description, due_at, status FROM tasks WHERE id = ?",
                (rs, rowNum) -> mapTask(rs.getLong("id"), rs.getLong("board_id"), rs.getString("title"),
                        rs.getString("course"), rs.getString("description"), rs.getString("due_at"), rs.getString("status")),
                taskId);
        return result.stream().findFirst();
    }

    public List<TaskItem> findByBoard(long boardId) {
        return jdbc.query(
                "SELECT id, board_id, title, course, description, due_at, status FROM tasks WHERE board_id = ?",
                (rs, rowNum) -> mapTask(rs.getLong("id"), rs.getLong("board_id"), rs.getString("title"),
                        rs.getString("course"), rs.getString("description"), rs.getString("due_at"), rs.getString("status")),
                boardId);
    }

    public List<TaskItem> findAssignedToUser(long userId) {
        return jdbc.query("""
                SELECT t.id, t.board_id, t.title, t.course, t.description, t.due_at, t.status
                FROM tasks t
                JOIN task_assignees ta ON ta.task_id = t.id
                WHERE ta.user_id = ?
                ORDER BY t.due_at
                """,
                (rs, rowNum) -> mapTask(rs.getLong("id"), rs.getLong("board_id"), rs.getString("title"),
                        rs.getString("course"), rs.getString("description"), rs.getString("due_at"), rs.getString("status")),
                userId);
    }

    public void updateStatus(long taskId, TaskStatus status) {
        jdbc.update("UPDATE tasks SET status = ? WHERE id = ?", status.name(), taskId);
    }


    public void update(long taskId, String title, String course,
                       String description, LocalDateTime dueAt) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }

        String sql = """
            UPDATE tasks
            SET title = ?, course = ?, description = ?, due_at = ?
            WHERE id = ?
            """;

        int rowsAffected = jdbc.update(
                sql,
                title.trim(),
                course,
                description,
                dueAt == null ? null : dueAt.toString(),
                taskId
        );

        if (rowsAffected == 0) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }
    }

    public void delete(long taskId) {
        int rowsAffected = jdbc.update(
                "DELETE FROM tasks WHERE id = ?",
                taskId
        );

        if (rowsAffected == 0) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }
    }

    @Transactional
    public void replaceAssignees(long taskId, List<Long> userIds) {
        jdbc.update("DELETE FROM task_assignees WHERE task_id = ?", taskId);
        for (Long userId : userIds) {
            jdbc.update("INSERT INTO task_assignees(task_id, user_id) VALUES (?, ?)", taskId, userId);
        }
    }

    public List<Long> findAssigneeIds(long taskId) {
        return jdbc.query("SELECT user_id FROM task_assignees WHERE task_id = ? ORDER BY user_id",
                (rs, rowNum) -> rs.getLong("user_id"), taskId);
    }

    public List<String> findAssigneeNames(long taskId) {
        return jdbc.query("""
                SELECT u.username
                FROM task_assignees ta
                JOIN users u ON u.id = ta.user_id
                WHERE ta.task_id = ?
                ORDER BY lower(u.username)
                """, (rs, rowNum) -> rs.getString("username"), taskId);
    }

    private TaskItem mapTask(long id, long boardId, String title, String course,
                             String description, String dueAtText, String statusText) {
        LocalDateTime dueAt = (dueAtText == null || dueAtText.isBlank()) ? null : LocalDateTime.parse(dueAtText);
        return new TaskItem(id, boardId, title, course == null ? "" : course,
                description == null ? "" : description, dueAt, TaskStatus.valueOf(statusText));
    }
}
