package com.happyvibe.service;

import com.happyvibe.model.BoardMember;
import com.happyvibe.model.Invitation;
import com.happyvibe.model.TaskItem;
import com.happyvibe.model.User;
import com.happyvibe.repository.BoardRepository;
import com.happyvibe.repository.InvitationRepository;
import com.happyvibe.repository.TaskRepository;
import com.happyvibe.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class CollaborationService {
    private final BoardService boardService;
    private final BoardRepository boards;
    private final InvitationRepository invitations;
    private final UserRepository users;
    private final TaskRepository tasks;
    private final AppSession session;
    private final Clock clock;

    public CollaborationService(BoardService boardService, BoardRepository boards,
                                InvitationRepository invitations, UserRepository users,
                                TaskRepository tasks, AppSession session, Clock clock) {
        this.boardService = boardService;
        this.boards = boards;
        this.invitations = invitations;
        this.users = users;
        this.tasks = tasks;
        this.session = session;
        this.clock = clock;
    }

    public void invite(long boardId, String username) {
        boardService.requireOwner(boardId);
        String clean = username == null ? "" : username.trim();
        User invitee = users.findByUsername(clean)
                .orElseThrow(() -> new IllegalArgumentException("That user is not registered."));
        if (invitee.id() == session.currentUser().id()) {
            throw new IllegalArgumentException("You are already the owner of this board.");
        }
        if (boards.isMember(boardId, invitee.id())) {
            throw new IllegalArgumentException("That user is already a board member.");
        }
        if (invitations.hasPending(boardId, invitee.id())) {
            throw new IllegalArgumentException("That user already has a pending invitation.");
        }
        invitations.create(boardId, session.currentUser().id(), invitee.id(), LocalDateTime.now(clock));
    }

    public List<Invitation> myPendingInvitations() {
        return invitations.findPendingForUser(session.currentUser().id());
    }

    @Transactional
    public void respondToInvitation(long invitationId, boolean accept) {
        long currentUserId = session.currentUser().id();
        Long inviteeId = invitations.findInviteeId(invitationId);
        Long boardId = invitations.findBoardId(invitationId);
        if (inviteeId == null || boardId == null) {
            throw new IllegalArgumentException("Invitation is no longer pending.");
        }
        if (inviteeId != currentUserId) {
            throw new IllegalStateException("This invitation does not belong to you.");
        }
        if (accept) {
            boards.addMember(boardId, currentUserId);
            invitations.updateStatus(invitationId, "ACCEPTED");
        } else {
            invitations.updateStatus(invitationId, "DECLINED");
        }
    }

    public List<BoardMember> boardMembers(long boardId) {
        boardService.requireMember(boardId);
        return boards.findMembers(boardId);
    }

    public void removeMember(long boardId, long userId) {
        boardService.requireOwner(boardId);
        if (userId == session.currentUser().id()) {
            throw new IllegalArgumentException("The board owner cannot remove themselves.");
        }
        boards.removeMember(boardId, userId);
    }

    public void assignTask(long taskId, List<Long> userIds) {
        TaskItem task = tasks.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found."));
        boardService.requireMember(task.boardId());

        List<Long> uniqueIds = new ArrayList<>(new LinkedHashSet<>(userIds == null ? List.of() : userIds));
        for (Long userId : uniqueIds) {
            if (!boards.isMember(task.boardId(), userId)) {
                throw new IllegalArgumentException("Every assignee must be a member of this board.");
            }
        }
        tasks.replaceAssignees(taskId, uniqueIds);
    }

    public List<Long> assigneeIds(long taskId) {
        return tasks.findAssigneeIds(taskId);
    }
}
