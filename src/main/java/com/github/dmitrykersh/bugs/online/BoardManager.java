package com.github.dmitrykersh.bugs.online;

import com.github.dmitrykersh.bugs.logic.board.AbstractBoard;
import com.github.dmitrykersh.bugs.logic.board.RectangleBoard;
import com.github.dmitrykersh.bugs.logic.board.layout.Layout;
import com.github.dmitrykersh.bugs.logic.board.validator.SimpleTurnValidator;
import com.github.dmitrykersh.bugs.logic.player.Player;
import com.github.dmitrykersh.bugs.logic.player.PlayerSettings;
import lombok.NoArgsConstructor;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class BoardManager {
    private static final Map<Session, Player> sessionToPlayer = Collections.synchronizedMap(new HashMap<>());

    public AbstractBoard getBoard(Session s) { return sessionToPlayer.get(s).getBoard(); }

    public AbstractBoard createBoard(final @NotNull Layout layout, final @NotNull String configName) {
        AbstractBoard board = null;
        switch (layout.getBoardType()) {
            case "RECT" : {
                board = RectangleBoard.createEmptyBoard(
                        layout,
                        configName,
                        SimpleTurnValidator.INSTANCE
                );
            }
        }
        return board;
    }

    public boolean connectToBoard(Session session, AbstractBoard board, int playerNumber, PlayerSettings playerSettings) {
        Player p;
        if ((p = board.tryAddPlayer(playerNumber, playerSettings)) != null) {
            sessionToPlayer.put(session, p);
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
}
