package com.github.dmitrykersh.bugs.api.board;

import com.github.dmitrykersh.bugs.api.player.Player;
import com.github.dmitrykersh.bugs.api.board.tile.Tile;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.List;
import java.util.Vector;

public interface Board {
    List<Player> getPlayers();
    void activateTiles(final @NotNull Player player);
    boolean tryMakeTurn(final @NotNull Player player, int tileId);
    void freezeLostPlayer(final @NotNull Player player);
    List<Tile> getNearbyTilesForPlayer(final @NotNull Tile origin, final @NotNull Player player);
    Player getActivePlayer();

    // for prototype testing
    void print (final @NotNull PrintStream ps, final @NotNull Player player);
}
