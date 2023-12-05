package com.github.dmitrykersh.bugs.engine.protocol;

import lombok.experimental.UtilityClass;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

import static com.github.dmitrykersh.bugs.engine.protocol.ProtocolConstants.*;

@UtilityClass
public class ProtocolUtils {
    public static void sendInfo(Session s, SessionState state, String msg) throws IOException {
        s.getRemote().sendString(String.format("{ \"%s\" : \"%s\", \"%s\" : \"%s\", \"message\" : \"%s\" }", MSG_TYPE_TAG, INFO_MSG_TYPE, STATE_TAG, state, msg));
    }

    public static void sendJsonData(Session s, String msgType, SessionState state, String key, String jsonValue) throws IOException {
        s.getRemote().sendString(String.format("{ \"%s\" : \"%s\", \"%s\" : \"%s\", \"%s\" : %s }", MSG_TYPE_TAG, msgType, STATE_TAG, state, key, jsonValue));
    }

}
