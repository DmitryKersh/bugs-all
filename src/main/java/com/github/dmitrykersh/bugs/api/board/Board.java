package com.github.dmitrykersh.bugs.api.board;

import com.github.dmitrykersh.bugs.api.board.observer.BoardObserver;
import com.github.dmitrykersh.bugs.api.player.Player;
import com.github.dmitrykersh.bugs.api.board.tile.Tile;
import javafx.scene.Group;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.List;
import java.util.Vector;

public interface Board {
    boolean ended();
    List<Player> getPlayers();
    List<Player> getScoreboard();
    Player getActivePlayer();
    void activateTiles(final @NotNull Player player);
    boolean tryMakeTurn(final @NotNull Player player, int tileId);
    void freezeLostPlayer(final @NotNull Player player);
    void setObserver(final @NotNull BoardObserver obs);

    Group buildGrid();

    // for prototype testing
    void print (final @NotNull PrintStream ps, final @NotNull Player player);
}
