package com.github.dmitrykersh.bugs.online;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.dmitrykersh.bugs.logic.board.AbstractBoard;
import com.github.dmitrykersh.bugs.logic.board.BoardState;
import com.github.dmitrykersh.bugs.logic.board.RectangleBoard;
import com.github.dmitrykersh.bugs.logic.board.layout.Layout;
import com.github.dmitrykersh.bugs.logic.board.observer.BoardObserver;
import com.github.dmitrykersh.bugs.logic.board.observer.TurnInfo;
import com.github.dmitrykersh.bugs.logic.board.validator.SimpleTurnValidator;
import com.github.dmitrykersh.bugs.logic.player.Player;
import com.github.dmitrykersh.bugs.logic.player.PlayerSettings;
import lombok.AllArgsConstructor;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.github.dmitrykersh.bugs.logic.board.BoardState.*;

public class BoardManager {
    @AllArgsConstructor
    private class OnlineBoardObserver implements BoardObserver {
        private AbstractBoard board;
        @Override
        public void onInitialization(List<Player> players) {

        }

        @Override
        public void onPlayerKicked(Player kickedPlayer) {

        }

        @Override
        public void onTurnMade(TurnInfo turnInfo) {

        }

        @Override
        public void onGameEnded(Map<Player, Integer> scoreboard) {

        }
    }

    private final Map<Session, Player> sessionToPlayer = new HashMap<>();
    private final Map<Integer, AbstractBoard> activeBoards = new HashMap<>();
    private final Map<String, String> layouts = new HashMap<>();
    private static int id = 0;
    private String layoutCachedStr = null;

    public BoardManager(String layoutDir) {
        val files = FileUtils.listFiles(new File(layoutDir), null, false);
        for (File f : files) {
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

    // id = 0 indicates error
    public int createBoard(final @NotNull String layoutName, final @NotNull String gameModeName, final @NotNull Map<String, Integer> layoutParams) {
        Layout layout = new Layout(layoutParams);
        if (!layouts.containsKey(layoutName)) {
            return 0;
        }
        layout.loadLayout(layouts.get(layoutName));

        AbstractBoard board = null;
        switch (layout.getBoardType()) {
            case "RECT" : {
                board = RectangleBoard.createEmptyBoard(
                        layout,
                        gameModeName,
                        SimpleTurnValidator.INSTANCE
                );
            }
        }
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

    public void disconnect(Session s) {
        Player p = sessionToPlayer.get(s);
        if (p != null) {
            p.getBoard().removePlayer(p);
        }
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

    public List<Session> prepareAndStartBoard(int id) {
        AbstractBoard b = activeBoards.get(id);
        if (b == null || !b.prepareBoard()) {
            return null;
        }
        b.startGame();
        b.addObserver(new OnlineBoardObserver(b));
        return getSessionsForBoard(id);
    }

    private List<Session> getSessionsForBoard(int boardId) {
        return getSessionsForBoard(activeBoards.get(boardId));
    }

    private List<Session> getSessionsForBoard(AbstractBoard b) {
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
}
