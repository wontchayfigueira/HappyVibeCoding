package com.happyvibe.repository;

import com.happyvibe.model.Invitation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class InvitationRepository {
    private final JdbcTemplate jdbc;

    public InvitationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void create(long boardId, long inviterId, long inviteeId, LocalDateTime createdAt) {
        jdbc.update("""
                INSERT INTO board_invitations(board_id, inviter_id, invitee_id, status, created_at)
                VALUES (?, ?, ?, 'PENDING', ?)
                """, boardId, inviterId, inviteeId, createdAt.toString());
    }

    public boolean hasPending(long boardId, long inviteeId) {
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM board_invitations
                WHERE board_id = ? AND invitee_id = ? AND status = 'PENDING'
                """, Integer.class, boardId, inviteeId);
        return count != null && count > 0;
    }

    public List<Invitation> findPendingForUser(long userId) {
        return jdbc.query("""
                SELECT i.id, i.board_id, b.name AS board_name, u.username AS inviter_username,
                       i.status, i.created_at
                FROM board_invitations i
                JOIN boards b ON b.id = i.board_id
                JOIN users u ON u.id = i.inviter_id
                WHERE i.invitee_id = ? AND i.status = 'PENDING'
                ORDER BY i.created_at DESC
                """,
                (rs, rowNum) -> new Invitation(
                        rs.getLong("id"),
                        rs.getLong("board_id"),
                        rs.getString("board_name"),
                        rs.getString("inviter_username"),
                        rs.getString("status"),
                        LocalDateTime.parse(rs.getString("created_at"))),
                userId);
    }

    public Long findInviteeId(long invitationId) {
        List<Long> result = jdbc.query(
                "SELECT invitee_id FROM board_invitations WHERE id = ? AND status = 'PENDING'",
                (rs, rowNum) -> rs.getLong("invitee_id"), invitationId);
        return result.stream().findFirst().orElse(null);
    }

    public Long findBoardId(long invitationId) {
        List<Long> result = jdbc.query(
                "SELECT board_id FROM board_invitations WHERE id = ? AND status = 'PENDING'",
                (rs, rowNum) -> rs.getLong("board_id"), invitationId);
        return result.stream().findFirst().orElse(null);
    }

    public void updateStatus(long invitationId, String status) {
        jdbc.update("UPDATE board_invitations SET status = ? WHERE id = ? AND status = 'PENDING'", status, invitationId);
    }
}
