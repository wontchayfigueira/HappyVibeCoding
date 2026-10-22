package com.happyvibe.repository;

import com.happyvibe.model.Board;
import com.happyvibe.model.BoardMember;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class BoardRepository {
    private final JdbcTemplate jdbc;

    public BoardRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public long create(String name, long ownerId) {
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO boards(name, owner_id) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setLong(2, ownerId);
            return ps;
        }, key);
        if (key.getKey() == null) {
            throw new IllegalStateException("Could not create board");
        }
        long boardId = key.getKey().longValue();
        jdbc.update("INSERT INTO board_members(board_id, user_id, role) VALUES (?, ?, 'OWNER')", boardId, ownerId);
        return boardId;
    }

    public Optional<Board> findById(long boardId) {
        List<Board> result = jdbc.query(
                "SELECT id, name, owner_id FROM boards WHERE id = ?",
                (rs, rowNum) -> new Board(rs.getLong("id"), rs.getString("name"), rs.getLong("owner_id")),
                boardId);
        return result.stream().findFirst();
    }

    public List<Board> findForUser(long userId) {
        return jdbc.query("""
                SELECT b.id, b.name, b.owner_id
                FROM boards b
                JOIN board_members bm ON bm.board_id = b.id
                WHERE bm.user_id = ?
                ORDER BY lower(b.name)
                """,
                (rs, rowNum) -> new Board(rs.getLong("id"), rs.getString("name"), rs.getLong("owner_id")),
                userId);
    }

    public boolean isMember(long boardId, long userId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM board_members WHERE board_id = ? AND user_id = ?",
                Integer.class, boardId, userId);
        return count != null && count > 0;
    }

    public boolean isOwner(long boardId, long userId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM board_members WHERE board_id = ? AND user_id = ? AND role = 'OWNER'",
                Integer.class, boardId, userId);
        return count != null && count > 0;
    }

    public void addMember(long boardId, long userId) {
        jdbc.update("INSERT OR IGNORE INTO board_members(board_id, user_id, role) VALUES (?, ?, 'MEMBER')", boardId, userId);
    }

    @Transactional
    public void removeMember(long boardId, long userId) {
        jdbc.update("""
                DELETE FROM task_assignees
                WHERE user_id = ? AND task_id IN (SELECT id FROM tasks WHERE board_id = ?)
                """, userId, boardId);
        jdbc.update("DELETE FROM board_members WHERE board_id = ? AND user_id = ? AND role <> 'OWNER'", boardId, userId);
    }

    public List<BoardMember> findMembers(long boardId) {
        return jdbc.query("""
                SELECT u.id, u.username, bm.role
                FROM board_members bm
                JOIN users u ON u.id = bm.user_id
                WHERE bm.board_id = ?
                ORDER BY CASE bm.role WHEN 'OWNER' THEN 0 ELSE 1 END, lower(u.username)
                """,
                (rs, rowNum) -> new BoardMember(rs.getLong("id"), rs.getString("username"), rs.getString("role")),
                boardId);
    }
}
