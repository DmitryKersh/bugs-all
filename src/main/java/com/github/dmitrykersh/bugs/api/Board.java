package com.github.dmitrykersh.bugs.api;

import java.io.PrintStream;
import java.util.List;
import java.util.Vector;

public interface Board {
    Vector<Player> getPlayers();
    void activateTiles();
    void freezeLostPlayer(Player player);
    List<Tile> getNearbyTilesForPlayer(Tile origin, Player player);

    // for prototype testing
    void print (PrintStream ps);
}
