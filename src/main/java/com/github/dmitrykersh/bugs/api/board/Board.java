package com.github.dmitrykersh.bugs.api.board;

import com.github.dmitrykersh.bugs.api.player.Player;
import com.github.dmitrykersh.bugs.api.board.tile.Tile;

import java.io.PrintStream;
import java.util.List;
import java.util.Vector;

public interface Board {
    List<Player> getPlayers();
    void activateTiles(Player player);
    boolean tryMakeTurn(Player player, int tileId);
    void freezeLostPlayer(Player player);
    List<Tile> getNearbyTilesForPlayer(Tile origin, Player player);

    // for prototype testing
    void print (PrintStream ps, Player player);
}
