package com.github.dmitrykersh.bugs.server.pojo;

import com.github.dmitrykersh.bugs.server.protocol.SessionState;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SessionInfo {
    SessionState state;
    String username;
    String nickname;
    int boardId;

    public SessionInfo() {
        state = SessionState.NEW_CONNECTION;
    }
}