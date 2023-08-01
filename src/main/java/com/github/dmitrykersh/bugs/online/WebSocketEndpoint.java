package com.github.dmitrykersh.bugs.online;

import com.github.dmitrykersh.bugs.logic.board.AbstractBoard;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;
import java.util.*;

import static com.github.dmitrykersh.bugs.online.SessionState.*;

@WebSocket
public class WebSocketEndpoint {
    private final BoardManager boardManager = new BoardManager();
    private final Map<Session, SessionState> stateMap = new HashMap<>();

    @OnWebSocketConnect
    public void clientConnected(Session session){
        stateMap.put(session, NEW_CONNECTION);
        System.out.println("New client connected: " + session.getRemoteAddress().toString());
    }

    @OnWebSocketClose
    public void clientClose(Session session){
        stateMap.remove(session);
        System.out.println("Client disconnected: " + session.getRemoteAddress().toString());
    }

    @OnWebSocketError
    public void clientError(Throwable err){
        System.out.println("Client error: " + err.toString());
    }

    @OnWebSocketMessage
    public void clientMessage(Session session, String message) throws IOException {
        SessionState state = stateMap.get(session);

        switch (state) {
            case IN_GAME -> {

            }
            case NEW_CONNECTION -> {

            }
            case LOGGED_IN -> {

            }
        }
    }
}