package com.github.dmitrykersh.bugs.server;

import lombok.experimental.UtilityClass;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

@UtilityClass
public class Utils {
    public static void sendError(Session s, SessionState state, String msg) throws IOException {
        s.getRemote().sendString(String.format("{ \"type\" : \"ERROR\", \"state\" : \"%s\", \"message\" : \"%s\" }", state, msg));
    }
    public static void sendInfo(Session s, SessionState state, String msg) throws IOException {
        s.getRemote().sendString(String.format("{ \"type\" : \"INFO\", \"state\" : \"%s\", \"message\" : \"%s\" }", state, msg));
    }

    public static void sendJsonData(Session s, SessionState state, String key, String jsonValue) throws IOException {
        s.getRemote().sendString(String.format("{ \"type\" : \"DATA\", \"state\" : \"%s\", \"%s\" : %s }", state, key, jsonValue));
    }

}
