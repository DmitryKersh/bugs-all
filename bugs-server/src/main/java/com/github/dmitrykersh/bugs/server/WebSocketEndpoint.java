package com.github.dmitrykersh.bugs.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dmitrykersh.bugs.engine.board.BoardInfo;
import com.github.dmitrykersh.bugs.engine.board.observer.BoardObserver;
import com.github.dmitrykersh.bugs.engine.board.observer.TurnInfo;
import com.github.dmitrykersh.bugs.engine.player.Player;
import com.github.dmitrykersh.bugs.engine.player.PlayerSettings;
import javafx.scene.paint.Color;
import lombok.Setter;
import lombok.val;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.dmitrykersh.bugs.server.SessionState.*;
import static com.github.dmitrykersh.bugs.server.Utils.*;
import static com.github.dmitrykersh.bugs.server.ProtocolConstants.*;

@WebSocket
public class WebSocketEndpoint {
    // СУКА ЕСЛИ НЕ КОННЕКТИТСЯ, ПОМЕНЯЙ ЭТО vvv
    private static final String LAYOUT_DIR = "C:\\Users\\TSP\\IdeaProjects\\bugs-all\\bugs-gui\\src\\main\\resources\\layout";
    private static final BoardManager boardManager = new BoardManager(LAYOUT_DIR);
    private static final Map<Session, SessionInfo> sessionInfoMap = new ConcurrentHashMap<>();
    private static final Map<String, Integer> userToOwnedBoard = new HashMap<>();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    private class OnlineBoardObserver implements BoardObserver {
        @Setter
        private Map<Player, Session> playerToSession = new HashMap<>();

        @Override
        public void onInitialization(List<Player> players) {
            for (Session s : playerToSession.values()) {
                try {
                    sendJsonData(s, "game_start", IN_GAME,"players", jsonMapper.writeValueAsString(players));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public void onPlayerKicked(Player kickedPlayer) {
            for (Session s : playerToSession.values()) {
                try {
                    sendJsonData(s, "kick", IN_GAME,"kicked_player", jsonMapper.writeValueAsString(kickedPlayer));

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            Session session = playerToSession.get(kickedPlayer);
            sessionInfoMap.get(session).setState(LOGGED_IN);
        }

        @Override
        public void onTurnMade(TurnInfo turnInfo) {
            for (Session s : playerToSession.values()) {
                try {
                    sendJsonData(s, "turn", IN_GAME,"turn_info", jsonMapper.writeValueAsString(turnInfo));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public void onGameEnded(Map<Player, Integer> scoreboard) {
            for (Session s : playerToSession.values()) {
                try {
                    sendJsonData(s, "game_end", LOGGED_IN,"scoreboard", jsonMapper.writeValueAsString(scoreboard));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

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
        SessionState currentState = sessionInfo.getState();
        val node = msgRoot.get(ACTION);
        String action = node != null ? node.asText() : "";
        switch (currentState) {
            case NEW_CONNECTION -> {
                String username = msgRoot.get(USERNAME).asText();
                String password = msgRoot.get(PASSWORD).asText();
                String nickname = msgRoot.get(NICKNAME).asText();

                if (userToOwnedBoard.containsKey(username)) {
                    sendInfo(session, sessionInfo.getState(), String.format("User %s is already connected", username));
                    break;
                }
                if (!authenticate(username, password)) {
                    sendInfo(session, currentState,"Login failed");
                    break;
                }

                sessionInfo.setState(LOGGED_IN);
                sessionInfo.setUsername(username);
                sessionInfo.setNickname(nickname);
                userToOwnedBoard.put(username, null);
                sendInfo(session, LOGGED_IN,String.format("Logged in as %s", username));
            }
            case LOGGED_IN -> {
                switch (action) {
                    case "create_board" -> {
                        String layoutName = msgRoot.get("layout_name").asText();
                        String configName = msgRoot.get("config_name").asText();
                        Map<String, Integer> layoutParams = jsonMapper.convertValue(msgRoot.get("params"), new TypeReference<>() {});
                        if (userToOwnedBoard.get(sessionInfo.getUsername()) != null) {
                            sendInfo(session, currentState, String.format("board already created by this client (id=%d)", userToOwnedBoard.get(sessionInfo.getUsername())));
                            return;
                        }

                        int boardId;
                        if ((boardId = boardManager.createBoard(layoutName, configName, layoutParams)) != 0) {
                            userToOwnedBoard.put(sessionInfo.getUsername(), boardId);
                            sendJsonData(session, "board_created", currentState,"created_board_id", String.valueOf(boardId));
                        }
                    }
                    case "connect_to_board" -> {
                        int boardId = msgRoot.get("board_id").asInt();
                        int playerNumber = msgRoot.get("player_number").asInt();
                        String colorStr = msgRoot.get("player_color").asText();

                        if (!boardManager.connectToBoard(session, boardId, playerNumber, new PlayerSettings(sessionInfo.getNickname(), Color.web(colorStr)))) {
                            sendInfo(session, currentState,"Error connecting to the board");
                            break;
                        }
                        sessionInfo.setState(CONNECTED_TO_BOARD);
                        sendInfo(session, CONNECTED_TO_BOARD,String.format("Connected to board %d", boardId));
                    }
                    case "get_board_info" -> {
                        int boardId = msgRoot.get("board_id").asInt();
                        BoardInfo boardInfo = boardManager.getBoardInfo(boardId);
                        if (boardInfo != null) {
                            sendJsonData(session,"board_info", currentState,"board_info", jsonMapper.writeValueAsString(boardInfo));
                        } else {
                            sendInfo(session, currentState, String.format("Board %d not found", boardId));
                        }
                    }
                    case "delete_board" -> {
                        int id;
                        List<Session> sessionsToLoggedIn;
                        if ((sessionsToLoggedIn = boardManager.deleteBoard(id = userToOwnedBoard.get(sessionInfo.getUsername()))) != null) {
                            sendInfo(session, LOGGED_IN,String.format("Deleted board with id %d", id));
                            for (Session s : sessionsToLoggedIn) {
                                sessionInfoMap.get(s).setState(LOGGED_IN);
                                sendInfo(s, LOGGED_IN,"The board you've been connected to was deleted");
                            }

                            userToOwnedBoard.put(sessionInfo.getUsername(), null);
                        } else {
                            sendInfo(session, currentState, String.format("cannot delete with id %d : game in progress or board does not exist", id));
                        }
                    }
                    case ACTION_LAYOUT_INFO -> {
                        sendJsonData(session, MSG_LAYOUT_INFO, currentState, MSG_LAYOUT_INFO_KEY, boardManager.getLayoutsAsJsonStr());
                    }
                    default -> {
                        sendInfo(session, currentState, String.format("Unexpected action: %s", action));
                    }
                }

            }
            case CONNECTED_TO_BOARD -> {
                switch (action) {
                    case "disconnect_from_board" -> {
                        boardManager.disconnect(session);
                        sessionInfo.setState(LOGGED_IN);
                        sendInfo(session, LOGGED_IN, "disconnected from board");
                    }
                    case "start_game" -> {
                        int boardId;
                        if (boardManager.getBoard(session) != boardManager.getBoard(boardId = userToOwnedBoard.get(sessionInfo.getUsername()))) {
                            sendInfo(session, currentState, String.format("Cannot start game. You're not owner of board you're connected to. Owned: %d", boardId));
                            break;
                        }
                        OnlineBoardObserver obs = new OnlineBoardObserver();
                        val playerSessionForBoard = boardManager.prepareAndStartBoard(boardId, obs);
                        if (playerSessionForBoard != null) {
                            for (val entry : playerSessionForBoard.entrySet()) {
                                sessionInfoMap.get(entry.getValue()).setState(IN_GAME);
                                sendJsonData(entry.getValue(), "game_started", IN_GAME,"start", jsonMapper.writeValueAsString(boardManager.getBoard(boardId)));
                            }
                            obs.setPlayerToSession(playerSessionForBoard);
                        } else {
                            sendInfo(session, currentState,"Cannot start game. Not all players are present");
                        }
                    }
                    default -> {
                        sendInfo(session, currentState, String.format("Unexpected action: %s", action));
                    }
                }

            }
            case IN_GAME -> {
                switch (action) {
                    case "make_turn" -> {
                        int tile_id = msgRoot.get("tile_id").asInt();
                        if (boardManager.tryMakeTurn(session, tile_id)) {
                            sendInfo(session, currentState,String.format("Turn made to tile %d", tile_id));
                        } else {
                            sendInfo(session, currentState,String.format("Cannot make turn to tile %d", tile_id));
                        }
                    }
                    default -> {
                        sendInfo(session, currentState, String.format("Unexpected action: %s", action));
                    }
                }
            }

        }
    }

    private boolean authenticate(String username, String password) {
        //TODO: implement
        return true;
    }
}