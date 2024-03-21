package com.github.dmitrykersh.bugs.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dmitrykersh.bugs.engine.board.BoardInfo;
import com.github.dmitrykersh.bugs.engine.board.TurnInfo;
import com.github.dmitrykersh.bugs.engine.board.observer.BoardObserver;
import com.github.dmitrykersh.bugs.engine.player.Player;
import com.github.dmitrykersh.bugs.engine.player.PlayerResult;
import com.github.dmitrykersh.bugs.engine.player.PlayerSettings;
import com.github.dmitrykersh.bugs.engine.protocol.SessionState;
import com.github.dmitrykersh.bugs.server.pojo.NotifyInfo;
import com.github.dmitrykersh.bugs.server.pojo.SessionInfo;
import javafx.scene.paint.Color;
import lombok.Setter;
import lombok.val;
import org.apache.commons.dbcp2.BasicDataSource;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.dmitrykersh.bugs.engine.protocol.ProtocolConstants.*;
import static com.github.dmitrykersh.bugs.engine.protocol.ProtocolUtils.sendInfo;
import static com.github.dmitrykersh.bugs.engine.protocol.ProtocolUtils.sendJsonData;
import static com.github.dmitrykersh.bugs.engine.protocol.SessionState.*;

@WebSocket
public class WebSocketEndpoint {
    public WebSocketEndpoint() {
        Yaml yaml = new Yaml();
        InputStream inputStream = WebSocketServer.class
                .getClassLoader()
                .getResourceAsStream("server_config/config.yaml");
        ServerConfig config = yaml.load(inputStream);
        if (boardManager == null)
            boardManager = new BoardManager(config.getLayoutDir());

        ds = new BasicDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl(config.getDbUrl());
        ds.setUsername(config.getDbUser());
        ds.setPassword(config.getDbPassword());
        ds.setMinIdle(5);
        ds.setMaxIdle(10);
        ds.setMaxOpenPreparedStatements(100);
    }
    private static BoardManager boardManager;
    private static final Map<Session, SessionInfo> sessionInfoMap = new ConcurrentHashMap<>();
    private static final Map<String, Integer> userToOwnedBoard = new HashMap<>();
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private static BasicDataSource ds;

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
        public void onGameEnded(List<PlayerResult> scoreboard) {
            changePlayerRatings(scoreboard);
            for (Session s : playerToSession.values()) {
                try {
                    sendJsonData(s, MSG_GAME_ENDED, LOGGED_IN, MSG_GAME_ENDED_KEY, jsonMapper.writeValueAsString(scoreboard));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private void changePlayerRatings(List<PlayerResult> scoreboard) {
            int playerAmount = scoreboard.size();
            int totalRating = 0;
            for (val plRes: scoreboard) {
                totalRating += plRes.getOldRating();
            }
            int avg = totalRating / playerAmount;

            List<Integer> baseGains = new ArrayList<>();
            switch (playerAmount) {
                case 2 -> {
                    baseGains.add(15);
                    baseGains.add(-15);
                }
                case 3 -> {
                    baseGains.add(20);
                    baseGains.add(-0);
                    baseGains.add(-20);
                }
                case 4 -> {
                    baseGains.add(25);
                    baseGains.add(-5);
                    baseGains.add(-5);
                    baseGains.add(-25);
                }
                default -> {
                    if (playerAmount % 2 == 0) {
                        for (int i = 0; i < playerAmount/2; i++)
                            baseGains.add(15 * (playerAmount / 2 - i) - 8);
                        for (int i = playerAmount/2; i < playerAmount; i++)
                            baseGains.add(15 * (playerAmount / 2 - i) + 8);
                    } else {
                        for (int i = 0; i < playerAmount; i++)
                            baseGains.add(15 * (playerAmount / 2 - i));
                    }
                }
            }

            // if draw then recalculate base_gains for drawed players
            int drawSize = scoreboard.stream().mapToInt(x -> x.getPlace() == 1 ? 1 : 0).sum();
            if (drawSize > 1) {
                int totalGain = 0;
                for (int i = 0; i < drawSize; i++) {
                    totalGain += baseGains.get(i);
                }
                // floor rounding here to avoid players gaining more rating at total because of draw
                int newBaseGain = totalGain / drawSize;
                for (int i = 0; i < drawSize; i++) {
                    baseGains.set(i, newBaseGain);
                }
            }

            for (val plRes: scoreboard) {
                int advantage = (plRes.getOldRating() - avg) / 25;
                int newRating = plRes.getOldRating() + baseGains.get(plRes.getPlace()-1) - advantage;

                plRes.setNewRating(newRating);
                plRes.setRatingChange(newRating - plRes.getOldRating());

                setRating(plRes.getUsername(), newRating);
                Session s = getSessionByUsername(plRes.getUsername());
                if (s != null) {
                    sessionInfoMap.get(s).setRating(newRating);
                }
            }
        }

        private Session getSessionByUsername(String username) {
            for (val entry : playerToSession.entrySet()) {
                if (entry.getKey().getUsername().equals(username)) return entry.getValue();
            }
            return null;
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
        NotifyInfo notifyInfo = boardManager.disconnectSession(session);
        if (notifyInfo.getBoardId() > 0) {
            String boardInfoStr = jsonMapper.writeValueAsString(boardManager.getBoardInfo(notifyInfo.getBoardId()));

            for (Session s : notifyInfo.getSessions()) {
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

        val node = msgRoot.get(ACTION_TAG);
        String action = node != null ? node.asText() : "";

        switch (currentState) {
            case NEW_CONNECTION -> {
                handleNewConnection(msgRoot, session, sessionInfo, currentState);
            }
            case LOGGED_IN -> {
                switch (action) {
                    case ACTION_CREATE_BOARD -> {
                        handleCreateBoard(msgRoot, session, sessionInfo, currentState);
                    }
                    case ACTION_CONNECT_TO_BOARD -> {
                        handleConnectToBoard(msgRoot, session, sessionInfo, currentState);
                    }
                    case ACTION_BOARD_INFO -> {
                        handleBoardInfo(msgRoot, session, sessionInfo, currentState);
                    }
                    case ACTION_DELETE_BOARD -> {
                        handleDeleteBoard(msgRoot, session, sessionInfo, currentState);
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
                        handleDisconnect(session, sessionInfo, currentState);
                    }
                    // reconnection to another slot
                    case ACTION_CONNECT_TO_BOARD -> {
                        handleDisconnect(session, sessionInfo, currentState);
                        handleConnectToBoard(msgRoot, session, sessionInfo, currentState);
                    }
                    case ACTION_START_GAME -> {
                        handleStartGame(msgRoot, session, sessionInfo, currentState);
                    }
                    default -> {
                        sendInfo(session, currentState, String.format("Unexpected action: %s", action));
                    }
                }

            }
            case IN_GAME -> {
                switch (action) {
                    case ACTION_MAKE_TURN -> {
                        handleMakeTurn(msgRoot, session, sessionInfo, currentState);
                    }
                    default -> {
                        sendInfo(session, currentState, String.format("Unexpected action: %s", action));
                    }
                }
            }
        }
    }

    private void handleMakeTurn(JsonNode msgRoot, Session session, SessionInfo sessionInfo, SessionState currentState) throws IOException {
        int tile_id = msgRoot.get(TILE_ID).asInt();
        if (boardManager.tryMakeTurn(session, tile_id)) {
            sendInfo(session, currentState, String.format("Turn made to tile %d", tile_id));
        } else {
            sendInfo(session, currentState, String.format("Cannot make turn to tile %d", tile_id));
        }
    }

    private void handleStartGame(JsonNode msgRoot, Session session, SessionInfo sessionInfo, SessionState currentState) throws IOException {
        int boardId;
        if (boardManager.getBoard(session) != boardManager.getBoard(boardId = userToOwnedBoard.get(sessionInfo.getUsername()))) {
            sendInfo(session, currentState, String.format("Cannot start game. You're not owner of board you're connected to. Owned: %d", boardId));
            return;
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

    private void handleDeleteBoard(JsonNode msgRoot, Session session, SessionInfo sessionInfo, SessionState currentState) throws IOException {
        Integer id;
        NotifyInfo notifyInfo;
        if (! userToOwnedBoard.containsKey(sessionInfo.getUsername()) || (id = userToOwnedBoard.get(sessionInfo.getUsername())) == null) return;

        if ((notifyInfo = boardManager.deleteBoard(id)) != null) {
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

    private void handleBoardInfo(JsonNode msgRoot, Session session, SessionInfo sessionInfo, SessionState currentState) throws IOException {
        int boardId = msgRoot.get(BOARD_ID).asInt();
        BoardInfo boardInfo = boardManager.getBoardInfo(boardId);
        if (boardInfo != null) {
            sendJsonData(session, MSG_BOARD_INFO, currentState, MSG_BOARD_INFO_KEY, jsonMapper.writeValueAsString(boardInfo));
            boardManager.setWatcher(session, boardId);
        } else {
            sendInfo(session, currentState, String.format("Board %d not found", boardId));
        }
    }

    private void handleConnectToBoard(JsonNode msgRoot, Session session, SessionInfo sessionInfo, SessionState currentState) throws IOException {
        int boardId = msgRoot.get(BOARD_ID).asInt();
        int playerNumber = msgRoot.get(PLAYER_NUMBER).asInt();
        String colorStr = msgRoot.get(PLAYER_COLOR).asText();

        if (!boardManager.connectToBoard(session, boardId, playerNumber, new PlayerSettings(sessionInfo.getUsername(), sessionInfo.getNickname(), Color.web(colorStr), sessionInfo.getRating()))) {
            sendInfo(session, currentState, "Error connecting to the board");
            return;
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

    private void handleDisconnect(Session session, SessionInfo sessionInfo, SessionState currentState) throws IOException {
        NotifyInfo notifyInfo = boardManager.disconnectFromBoard(session);
        sessionInfo.setState(LOGGED_IN);
        sendInfo(session, LOGGED_IN, "disconnected from board");
        String boardInfoStr = jsonMapper.writeValueAsString(boardManager.getBoardInfo(sessionInfo.getBoardId()));
        for (Session s : notifyInfo.getSessions()) {
            sendJsonData(s, MSG_BOARD_INFO, currentState, MSG_BOARD_INFO_KEY, boardInfoStr);
        }
    }

    private void handleNewConnection(JsonNode msgRoot, Session session, SessionInfo sessionInfo, SessionState currentState) throws IOException {
        if (!msgRoot.hasNonNull(USERNAME) || !msgRoot.hasNonNull(PASSWORD) || !msgRoot.hasNonNull(NICKNAME)) {
            return;
        }
        String username = msgRoot.get(USERNAME).asText();
        String password = msgRoot.get(PASSWORD).asText();
        String nickname = msgRoot.get(NICKNAME).asText();

        if (!authenticate(username, password)) {
            sendInfo(session, NEW_CONNECTION, "Login failed");
            return;
        }

        sessionInfo.setRating(getRating(username));

        for (SessionInfo info : sessionInfoMap.values()) {
            if (username.equals(info.getUsername())) {
                sendInfo(session, sessionInfo.getState(), String.format("User %s is already connected", username));
                return;
            }
        }

        if (userToOwnedBoard.containsKey(username)) {
            Integer boardId = userToOwnedBoard.get(username);
            if (boardId != null && boardId != 0) {
                sendInfo(session, sessionInfo.getState(), String.format("User %s is already owning board %d", username, boardId));
                sendJsonData(session, MSG_LOGIN_DATA, LOGGED_IN, MSG_LOGIN_DATA_KEY, String.format("{\"board_id\" : %d}", boardId));
            }
        } else {
            userToOwnedBoard.put(username, null);
        }

        sessionInfo.setState(LOGGED_IN);
        sessionInfo.setUsername(username);
        sessionInfo.setNickname(nickname);

        sendInfo(session, LOGGED_IN, String.format("Logged in as %s (%d)", username, sessionInfo.getRating()));
    }

    private void handleCreateBoard(JsonNode msgRoot, Session session, SessionInfo sessionInfo, SessionState currentState) throws IOException {
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

    private boolean authenticate(String username, String password) {
        try {
            val conn = ds.getConnection();

            PreparedStatement saltStmt = conn.prepareStatement("select salt from users where username = ? limit 1");
            saltStmt.setString(1, username);
            val saltRs = saltStmt.executeQuery();
            if (!saltRs.next()) return false;

            val salt = saltRs.getString("salt");

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedHashAsHex = bytesToHex(digest.digest((password + salt).getBytes(StandardCharsets.UTF_8)));

            PreparedStatement authStmt = conn.prepareStatement("select count(*) as record_count from users where username = ? and password_hash = ?");
            authStmt.setString(1, username);
            authStmt.setString(2, saltedHashAsHex);
            val authRs = authStmt.executeQuery();
            if (!authRs.next()) return false;
            boolean res = authRs.getInt("record_count") > 0;
            conn.close();
            return res;
        } catch (SQLException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int getRating(String username) {
        try {
            val conn = ds.getConnection();

            PreparedStatement stmt = conn.prepareStatement("select get_classic_elo(?)");
            stmt.setString(1, username);
            val rs = stmt.executeQuery();

            if (!rs.next()) return 0;
            int res = rs.getInt(1);
            conn.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    private boolean setRating(String username, int new_rating) {
        try {
            val conn = ds.getConnection();

            PreparedStatement stmt = conn.prepareStatement("call set_classic_elo(?, ?);");
            stmt.setString(1, username);
            stmt.setInt(2, new_rating);

            val rs = stmt.executeUpdate();
            conn.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}