package com.github.dmitrykersh.bugs.api;

import java.util.Vector;

public interface IBoard {
    Vector<IPlayer> getPlayers();
    void validateTurn(IPlayer attacker, Tile tile);
    void activateTiles();
    void freezeLostPlayer(IPlayer player);
}
