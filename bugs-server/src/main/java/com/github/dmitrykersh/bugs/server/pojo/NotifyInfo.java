package com.github.dmitrykersh.bugs.server.pojo;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.eclipse.jetty.websocket.api.Session;

import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotifyInfo {
    List<Session> sessions;
    int boardId;
}
