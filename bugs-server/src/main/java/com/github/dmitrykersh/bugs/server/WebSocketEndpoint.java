package com.github.dmitrykersh.bugs.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dmitrykersh.bugs.engine.board.BoardInfo;
import com.github.dmitrykersh.bugs.engine.board.observer.BoardObserver;
import com.github.dmitrykersh.bugs.engine.board.TurnInfo;
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
    private static final String LAYOUT_DIR = "C:\\Users\\dkarp\\IdeaProjects\\bugs-client\\bugs-gui\\src\\main\\resources\\layout";
    private static final BoardManager boardManager = new BoardManager(LAYOUT_DIR);
    private static final Map<Session, SessionInfo> sessionInfoMap = new ConcurrentHashMap<>();
    private static final Map<String, Integer> userToOwnedBoard = new HashMap<>();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    private class OnlineBoardObserver implements BoardObserver {
        @Setter
        private Map<Player, Session> playerToSession = new HashMap<>();

        @Override
        public void onInitialization(List<Player> players) {
            /*
            for (Session s : playerToSession.values()) {
                try {
                    sendJsonData(s, "game_start", IN_GAME,"players", jsonMapper.writeValueAsString(players));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            */
        }

        @Override
        public void onPlayerKicked(Player kickedPlayer) {
            for (Session s : playerToSession.values()) {
                try {
                    sendJsonData(s, MSG_PLAYER_KICKED, IN_GAME, MSG_PLAYER_KICKED_KEY, jsonMapper.writeValueAsString(kickedPlayer));

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
                    sendJsonData(s, MSG_TURN_MADE, IN_GAME, MSG_TURN_MADE_KEY, jsonMapper.writeValueAsString(turnInfo));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public void onGameEnded(Map<Integer, List<Player>> scoreboard) {
            for (Session s : playerToSession.values()) {
                try {
                    sendJsonData(s, MSG_GAME_ENDED, LOGGED_IN, MSG_GAME_ENDED_KEY, jsonMapper.writeValueAsString(scoreboard));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @OnWebSocketConnect
    public void clientConnected(Session session) {
        sessionInfoMap.put(session, new SessionInfo());
        System.out.println("New client connected: " + session.getRemoteAddress().toString());
    }

    @OnWebSocketClose
    public void clientClose(Session s) throws IOException {
        disconnectClient(s);
        System.out.println("Client disconnected: " + s.getRemoteAddress().toString());
    }

    @OnWebSocketError
    public void clientError(Session s, Throwable err) throws IOException {
        disconnectClient(s);
        System.out.println("Client error: ");
        err.printStackTrace();
    }

    private void disconnectClient(Session session) throws IOException {
        //userToOwnedBoard.remove(sessionInfoMap.get(session).getUsername());
        NotifyInfo sessionsToNotify = boardManager.disconnectSession(session);
        //sendInfo(session, LOGGED_IN, "disconnected from board");
        if (sessionsToNotify.getBoardId() > 0) {
            String boardInfoStr = jsonMapper.writeValueAsString(boardManager.getBoardInfo(sessionsToNotify.getBoardId()));

            for (Session s : sessionsToNotify.getSessions()) {
                sendJsonData(s, MSG_BOARD_INFO, sessionInfoMap.get(s).getState(), MSG_BOARD_INFO_KEY, boardInfoStr);
            }
        }
        sessionInfoMap.remove(session);
    }

    @OnWebSocketMessage
    public void clientMessage(Session session, String message) throws IOException {
        System.out.println(message);
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

                if (!authenticate(username, password)) {
                    sendInfo(session, currentState, "Login failed");
                    break;
                }

                for (SessionInfo info : sessionInfoMap.values()) {
                    if (username.equals(info.getUsername())) {
                        sendInfo(session, sessionInfo.getState(), String.format("User %s is already connected", username));
                        return;
                    }
                }

                if (userToOwnedBoard.containsKey(username)) {
                    int boardId = userToOwnedBoard.get(username);
                    sendInfo(session, sessionInfo.getState(), String.format("User %s is already owning board %d", username, boardId));
                    sendJsonData(session, MSG_LOGIN_DATA, LOGGED_IN, MSG_LOGIN_DATA_KEY, String.format("{\"board_id\" : %d}", boardId));
                } else {
                    userToOwnedBoard.put(username, null);
                }

                sessionInfo.setState(LOGGED_IN);
                sessionInfo.setUsername(username);
                sessionInfo.setNickname(nickname);

                sendInfo(session, LOGGED_IN, String.format("Logged in as %s", username));
            }
            case LOGGED_IN -> {
                switch (action) {
                    case ACTION_CREATE_BOARD -> {
                        String layoutName = msgRoot.get(LAYOUT_NAME).asText();
                        String gameMode = msgRoot.get(GAME_MODE).asText();
                        Map<String, Integer> layoutParams = jsonMapper.convertValue(msgRoot.get(LAYOUT_PARAMS), new TypeReference<>() {
                        });
                        if (userToOwnedBoard.get(sessionInfo.getUsername()) != null) {
                            sendInfo(session, currentState, String.format("board already created by this client (id=%d)", userToOwnedBoard.get(sessionInfo.getUsername())));
                            return;
                        }

                        int boardId;
                        if ((boardId = boardManager.createBoard(layoutName, gameMode, layoutParams)) != 0) {
                            userToOwnedBoard.put(sessionInfo.getUsername(), boardId);
                            sendInfo(session, currentState, String.format("Created board (id=%d)", boardId));
                            sendJsonData(session, MSG_BOARD_CREATED, currentState, MSG_BOARD_CREATED_ID, String.valueOf(boardId));
                        }
                    }
                    case ACTION_CONNECT_TO_BOARD -> {
                        int boardId = msgRoot.get(BOARD_ID).asInt();
                        int playerNumber = msgRoot.get(PLAYER_NUMBER).asInt();
                        String colorStr = msgRoot.get(PLAYER_COLOR).asText();

                        if (!boardManager.connectToBoard(session, boardId, playerNumber, new PlayerSettings(sessionInfo.getNickname(), Color.web(colorStr)))) {
                            sendInfo(session, currentState, "Error connecting to the board");
                            break;
                        }
                        sessionInfo.setState(CONNECTED_TO_BOARD);
                        sessionInfo.setBoardId(boardId);
                        sendInfo(session, CONNECTED_TO_BOARD, String.format("Connected to board %d", boardId));
                        String boardInfoStr = jsonMapper.writeValueAsString(boardManager.getBoardInfo(boardId));
                        List<Session> toNotify = boardManager.getSessionsForBoard(boardId);
                        toNotify.addAll(boardManager.getWatchers(boardId));
                        for (Session s : toNotify) {
                            sendJsonData(s, MSG_BOARD_INFO, sessionInfoMap.get(s).getState(), MSG_BOARD_INFO_KEY, boardInfoStr);
                        }
                    }
                    case ACTION_BOARD_INFO -> {
                        int boardId = msgRoot.get(BOARD_ID).asInt();
                        BoardInfo boardInfo = boardManager.getBoardInfo(boardId);
                        if (boardInfo != null) {
                            sendJsonData(session, MSG_BOARD_INFO, currentState, MSG_BOARD_INFO_KEY, jsonMapper.writeValueAsString(boardInfo));
                            boardManager.setWatcher(session, boardId);
                        } else {
                            sendInfo(session, currentState, String.format("Board %d not found", boardId));
                        }
                    }
                    case ACTION_DELETE_BOARD -> {
                        int id;
                        NotifyInfo notifyInfo;
                        if ((notifyInfo = boardManager.deleteBoard(id = userToOwnedBoard.get(sessionInfo.getUsername()))) != null) {
                            sendInfo(session, LOGGED_IN, String.format("Deleted board with id %d", id));
                            for (Session s : notifyInfo.getSessions()) {
                                sessionInfoMap.get(s).setState(LOGGED_IN);
                                sendInfo(s, LOGGED_IN, "The board you've been connected to was deleted");
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
                    case ACTION_DISCONNECT -> {
                        NotifyInfo notifyInfo = boardManager.disconnectFromBoard(session);
                        sessionInfo.setState(LOGGED_IN);
                        sendInfo(session, LOGGED_IN, "disconnected from board");
                        String boardInfoStr = jsonMapper.writeValueAsString(boardManager.getBoardInfo(sessionInfo.getBoardId()));
                        for (Session s : notifyInfo.getSessions()) {
                            sendJsonData(s, MSG_BOARD_INFO, currentState, MSG_BOARD_INFO_KEY, boardInfoStr);
                        }
                    }
                    case ACTION_START_GAME -> {
                        int boardId;
                        if (boardManager.getBoard(session) != boardManager.getBoard(boardId = userToOwnedBoard.get(sessionInfo.getUsername()))) {
                            sendInfo(session, currentState, String.format("Cannot start game. You're not owner of board you're connected to. Owned: %d", boardId));
                            break;
                        }
                        OnlineBoardObserver obs = new OnlineBoardObserver();
                        val playerSessionForBoard = boardManager.prepareAndStartBoard(boardId, obs);
                        if (playerSessionForBoard != null) {
                            String boardDtoStr = jsonMapper.writeValueAsString(boardManager.getBoard(boardId).makeDto());
                            for (val entry : playerSessionForBoard.entrySet()) {
                                sessionInfoMap.get(entry.getValue()).setState(IN_GAME);
                                sendJsonData(entry.getValue(), MSG_GAME_STARTED, IN_GAME, MSG_GAME_STARTED_KEY, boardDtoStr);
                            }
                            obs.setPlayerToSession(playerSessionForBoard);
                        } else {
                            sendInfo(session, currentState, "Cannot start game. Not all players are present");
                        }
                    }
                    default -> {
                        sendInfo(session, currentState, String.format("Unexpected action: %s", action));
                    }
                }

            }
            case IN_GAME -> {
                switch (action) {
                    case ACTION_MAKE_TURN -> {
                        int tile_id = msgRoot.get(TILE_ID).asInt();
                        if (boardManager.tryMakeTurn(session, tile_id)) {
                            sendInfo(session, currentState, String.format("Turn made to tile %d", tile_id));
                        } else {
                            sendInfo(session, currentState, String.format("Cannot make turn to tile %d", tile_id));
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