package com.github.dmitrykersh.bugs.online;

import lombok.experimental.UtilityClass;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

@UtilityClass
public class Utils {
    public void sendError(Session s, String msg) throws IOException {
        s.getRemote().sendString(String.format("{ \"type\" : \"ERROR\", \"message\" : \"%s\" }", msg));
    }
    public void sendInfo(Session s, String msg) throws IOException {
        s.getRemote().sendString(String.format("{ \"type\" : \"INFO\", \"message\" : \"%s\" }", msg));
    }

    public void sendJsonData(Session s, String key, String jsonValue) throws IOException {
        s.getRemote().sendString(String.format("{ \"type\" : \"DATA\", \"%s\" : %s }", key, jsonValue));
    }
}
