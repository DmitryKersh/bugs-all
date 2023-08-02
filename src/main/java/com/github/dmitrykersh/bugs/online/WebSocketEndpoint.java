package com.github.dmitrykersh.bugs.online;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dmitrykersh.bugs.logic.player.PlayerSettings;
import javafx.scene.paint.Color;
import lombok.val;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;
import java.util.*;

import static com.github.dmitrykersh.bugs.online.SessionState.*;

@WebSocket
public class WebSocketEndpoint {
    private static final String LAYOUT_DIR = "C:\\Users\\dkarpukhin\\MINE\\bugs-client\\src\\main\\resources\\layout";
    private static final BoardManager boardManager = new BoardManager(LAYOUT_DIR);
    private final Map<Session, SessionInfo> sessionInfoMap = new HashMap<>();
    private final Map<String, Integer> userToOwnedBoard = new HashMap<>();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    @OnWebSocketConnect
    public void clientConnected(Session session){
        sessionInfoMap.put(session, new SessionInfo());
        System.out.println("New client connected: " + session.getRemoteAddress().toString());
    }

    @OnWebSocketClose
    public void clientClose(Session session){
        boardManager.disconnect(session);
        userToOwnedBoard.remove(sessionInfoMap.get(session).getUsername());
        sessionInfoMap.remove(session);
        System.out.println("Client disconnected: " + session.getRemoteAddress().toString());
    }

    @OnWebSocketError
    public void clientError(Throwable err){
        System.out.println("Client error: ");
        err.printStackTrace();
    }

    @OnWebSocketMessage
    public void clientMessage(Session session, String message) throws IOException {
        SessionInfo sessionInfo = sessionInfoMap.get(session);
        JsonNode msgRoot = jsonMapper.readTree(message);

        switch (sessionInfo.getState()) {
            case NEW_CONNECTION -> {
                String username = msgRoot.get("username").asText();
                String password = msgRoot.get("password").asText();
                String nickname = msgRoot.get("nickname").asText();

                if (userToOwnedBoard.containsKey(username)) {
                    sendError(session, String.format("user %s is already connected", username));
                    break;
                }
                if (!authenticate(username, password)) {
                    sendError(session, "login failed");
                    break;
                }

                sessionInfo.setState(LOGGED_IN);
                sessionInfo.setUsername(username);
                sessionInfo.setNickname(nickname);
                userToOwnedBoard.put(username, null);
                sendInfo(session, String.format("logged in as %s", username));
            }
            case LOGGED_IN -> {
                switch (msgRoot.get("action").asText()) {
                    case "create_board" -> {
                        String layoutName = msgRoot.get("layout_name").asText();
                        String configName = msgRoot.get("config_name").asText();
                        Map<String, Integer> layoutParams = jsonMapper.convertValue(msgRoot.get("params"), new TypeReference<>() {});
                        if (userToOwnedBoard.get(sessionInfo.getUsername()) != null) {
                            sendError(session, String.format("board already created by this client (id=%d)", userToOwnedBoard.get(sessionInfo.getUsername())));
                        }

                        int boardId;
                        if ((boardId = boardManager.createBoard(layoutName, configName, layoutParams)) != 0) {
                            sendJsonData(session, "board_id", String.valueOf(boardId));
                        }
                    }
                    case "connect_to_board" -> {
                        int boardId = msgRoot.get("board_id").asInt();
                        int playerNumber = msgRoot.get("player_number").asInt();
                        String colorStr = msgRoot.get("player_color").asText();

                        if (!boardManager.connectToBoard(session, boardId, playerNumber, new PlayerSettings(sessionInfo.getNickname(), Color.web(colorStr)))) {
                            sendError(session, "error connecting to the board");
                            break;
                        }
                        sessionInfo.setState(CONNECTED_TO_BOARD);
                        sendInfo(session, String.format("connected to board %d", boardId));
                    }
                    case "delete_board" -> {
                        int id;
                        List<Session> sessionsToLoggedIn;
                        if ((sessionsToLoggedIn = boardManager.deleteBoard(id = userToOwnedBoard.get(sessionInfo.getUsername()))) != null) {
                            sendInfo(session, String.format("deleted board with id %d", id));
                            for (Session s : sessionsToLoggedIn) {
                                sessionInfoMap.get(s).setState(LOGGED_IN);
                                sendInfo(s, "the board you've been connected to was deleted");
                            }

                            userToOwnedBoard.put(sessionInfo.getUsername(), null);
                        } else {
                            sendError(session, String.format("cannot delete with id %d : game in progress or board does not exist", id));
                        }
                    }
                    case "get_layout_info" -> {
                        sendJsonData(session, "layouts", boardManager.getLayoutsAsJsonStr());
                    }
                }

            }
            case CONNECTED_TO_BOARD -> {
                switch (msgRoot.get("action").asText()) {
                    case "disconnect_from_board" -> {
                        boardManager.disconnect(session);
                        sessionInfo.setState(LOGGED_IN);
                    }
                }
            }
            case IN_GAME -> {
                break;
            }

        }
    }

    private void sendError(Session s, String msg) throws IOException {
        s.getRemote().sendString(String.format("{ \"type\" : \"ERROR\", \"message\" : \"%s\" }", msg));
    }
    private void sendInfo(Session s, String msg) throws IOException {
        s.getRemote().sendString(String.format("{ \"type\" : \"INFO\", \"message\" : \"%s\" }", msg));
    }

    private void sendJsonData(Session s, String key, String jsonValue) throws IOException {
        s.getRemote().sendString(String.format("{ \"type\" : \"DATA\", \"%s\" : %s }", key, jsonValue));
    }

    private boolean authenticate(String username, String password) {
        //TODO: implement
        return true;
    }
}