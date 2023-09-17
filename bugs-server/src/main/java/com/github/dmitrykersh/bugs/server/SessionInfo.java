package com.github.dmitrykersh.bugs.server;

import lombok.Data;

@Data
public class SessionInfo {
    private SessionState state;
    private String username;
    private String nickname;
    private int boardId;

    public SessionInfo() {
        state = SessionState.NEW_CONNECTION;
    }
}
