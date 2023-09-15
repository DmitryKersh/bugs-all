package com.github.dmitrykersh.bugs.server;

import lombok.experimental.UtilityClass;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

@UtilityClass
public class Utils {
    public static void sendInfo(Session s, SessionState state, String msg) throws IOException {
        s.getRemote().sendString(String.format("{ \"type\" : \"INFO\", \"state\" : \"%s\", \"message\" : \"%s\" }", state, msg));
    }

    public static void sendJsonData(Session s, String msgType, SessionState state, String key, String jsonValue) throws IOException {
        s.getRemote().sendString(String.format("{ \"type\" : \"%s\", \"state\" : \"%s\", \"%s\" : %s }", msgType, state, key, jsonValue));
    }

}
