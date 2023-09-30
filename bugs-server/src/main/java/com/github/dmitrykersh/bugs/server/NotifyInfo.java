package com.github.dmitrykersh.bugs.server;

import lombok.Builder;
import lombok.Data;
import org.eclipse.jetty.websocket.api.Session;

import java.util.List;

@Data
@Builder
public class NotifyInfo {
    private List<Session> sessions;
    private int boardId;
}
