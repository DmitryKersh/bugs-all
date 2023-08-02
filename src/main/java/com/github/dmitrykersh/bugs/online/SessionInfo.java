package com.github.dmitrykersh.bugs.online;

import lombok.Data;

@Data
public class SessionInfo {
    private SessionState state;
    private String username;
    private String nickname;

    public SessionInfo() {
        state = SessionState.NEW_CONNECTION;
    }
}
