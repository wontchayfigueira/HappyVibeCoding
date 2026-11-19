package com.happyvibe.service;

import com.happyvibe.repository.BoardRepository;
import org.springframework.stereotype.Service;

@Service
public class BoardService {

    private final BoardRepository boards;
    private final AppSession session;

    public BoardService(BoardRepository boards, AppSession session) {
        this.boards = boards;
        this.session = session;
    }

    public void requireOwner(long boardId) {
        long userId = session.currentUser().id();

        if (!boards.isOwner(boardId, userId)) {
            throw new IllegalStateException(
                    "You must be the board owner to perform this action."
            );
        }
    }

    public void requireMember(long boardId) {
        long userId = session.currentUser().id();

        if (!boards.isMember(boardId, userId)) {
            throw new IllegalStateException(
                    "You must be a member of this board to perform this action."
            );
        }
    }
}

