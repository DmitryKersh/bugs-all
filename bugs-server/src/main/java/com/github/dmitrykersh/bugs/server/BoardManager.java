package com.github.dmitrykersh.bugs.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.dmitrykersh.bugs.engine.board.AbstractBoard;
import com.github.dmitrykersh.bugs.engine.board.BoardInfo;
import com.github.dmitrykersh.bugs.engine.board.RectangleBoard;
import com.github.dmitrykersh.bugs.engine.board.layout.Layout;
import com.github.dmitrykersh.bugs.engine.board.observer.BoardObserver;
import com.github.dmitrykersh.bugs.engine.board.validator.SimpleTurnValidator;
import com.github.dmitrykersh.bugs.engine.player.Player;
import com.github.dmitrykersh.bugs.engine.player.PlayerSettings;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.dmitrykersh.bugs.engine.board.BoardState.ENDED;
import static com.github.dmitrykersh.bugs.engine.board.BoardState.NOT_STARTED;

public class BoardManager {
    private final Map<Session, Player> sessionToPlayer = new HashMap<>();
    private final Map<Integer, AbstractBoard> activeBoards = new HashMap<>();
    private final Map<String, String> layouts = new HashMap<>();
    private static int id = 0;
    private String layoutCachedStr = null;

    public BoardManager(String layoutDir) {
        val files = FileUtils.listFiles(new File(layoutDir), null, false);
        for (val f : files) {
            try {
                layouts.put(f.getName(), FileUtils.readFileToString(f, "utf-8"));
                System.out.printf("Layout %s read\n", f.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public AbstractBoard getBoard(Session s) { return sessionToPlayer.get(s).getBoard(); }
    public AbstractBoard getBoard(int id) { return activeBoards.get(id); }

    public BoardInfo getBoardInfo(int id) {
        AbstractBoard b = getBoard(id);
        if (b == null) return null;
        return b.getInfo();
    }

    // id = 0 indicates error
    public int createBoard(final @NotNull String layoutName, final @NotNull String gameModeName, final @NotNull Map<String, Integer> layoutParams) {
        Layout layout = new Layout(layoutParams);
        /*if (!layouts.containsKey(layoutName)) {
            return 0;
        }*/
        boolean found = false;
        for (val entry : layouts.entrySet()) {
            if (new JSONObject(entry.getValue()).getString("name").equals(layoutName)) {
                found = true;
                layout.loadLayout(entry.getValue());
            }
        }
        if (!found) return 0;
        //layout.loadLayout(layouts.get(layoutName));

        AbstractBoard board = switch (layout.getBoardType()) {
            case "RECT" -> RectangleBoard.createEmptyBoard(
                layout,
                gameModeName,
                SimpleTurnValidator.INSTANCE
            );
            default -> null;
        };
        id++;
        activeBoards.put(id, board);
        return id;
    }

    // returns sessions for which to change status
    public List<Session> deleteBoard(int id) {
        AbstractBoard b = activeBoards.get(id);

        if (b.getState() == ENDED || b.getState() == NOT_STARTED) {
            List<Session> sessions = getSessionsForBoard(b);
            for (int i = 0; i < b.getPlayers().size(); i++) {
                // does not change size of player vector!
                b.removePlayer(i+1);
            }

            activeBoards.remove(id);
            return sessions;
        }
        return null;
    }

    public void getBoardSnapshot(int id) {
        AbstractBoard b = getBoard(id);

    }

    public boolean connectToBoard(Session session, int boardId, int playerNumber, PlayerSettings playerSettings) {
        Player player;
        AbstractBoard board = activeBoards.get(boardId);
        if (board == null) return false;
        if ((player = board.tryAddPlayer(playerNumber, playerSettings)) != null) {
            sessionToPlayer.put(session, player);
            return true;
        }
        return false;
    }

    public List<Session> disconnect(Session s) {
        Player p = sessionToPlayer.get(s);
        AbstractBoard b;
        if (p != null) {
            b = p.getBoard();
            List<Session> toNotify = getSessionsForBoard(b);
            p.getBoard().removePlayer(p);
            return toNotify;
        }
        return new ArrayList<>();
    }

    public String getLayoutsAsJsonStr() throws JsonProcessingException {
        if (layoutCachedStr != null) return layoutCachedStr;

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode root = mapper.createArrayNode();

        for (val entry : layouts.entrySet()) {
            String layoutJsonStr = String.format("{\"name\" : \"%s\", \"layout\" : %s }", entry.getKey(), entry.getValue());
            root.add(mapper.readTree(layoutJsonStr));
        }
        layoutCachedStr = root.toString();
        return layoutCachedStr;
    }

    public Map<Player, Session> prepareAndStartBoard(int id, BoardObserver obs) {
        AbstractBoard b = activeBoards.get(id);
        if (b == null || !b.prepareBoard() || !b.startGame()) {
            return null;
        }
        b.addObserver(obs);
        return getPlayerToSessionForBoard(id);
    }

    public boolean tryMakeTurn(Session session, int tileId) {
        Player p = sessionToPlayer.get(session);
        if (p == null)
            return false;
        return p.tryMakeTurn(tileId);
    }

    public List<Session> getSessionsForBoard(int boardId) {
        return getSessionsForBoard(activeBoards.get(boardId));
    }

    public List<Session> getSessionsForBoard(AbstractBoard b) {
        List<Session> sessions = new ArrayList<>();
        if (b == null)
            return sessions;
        for (val entry : sessionToPlayer.entrySet()) {
            if (b.getPlayers().contains(entry.getValue())) {
                sessions.add(entry.getKey());
            }
        }
        return sessions;
    }

    private Map<Player, Session> getPlayerToSessionForBoard(AbstractBoard b) {
        Map<Player, Session> p2s = new HashMap<>();
        if (b == null)
            return p2s;
        for (val entry : sessionToPlayer.entrySet()) {
            if (b.getPlayers().contains(entry.getValue())) {
                p2s.put(entry.getValue(), entry.getKey());
            }
        }
        return p2s;
    }
    private Map<Player, Session> getPlayerToSessionForBoard(int board_id) {
        return getPlayerToSessionForBoard(activeBoards.get(board_id));
    }
}
