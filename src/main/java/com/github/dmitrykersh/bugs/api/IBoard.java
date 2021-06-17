package com.github.dmitrykersh.bugs.api;

public interface IBoard {
    void validateAttack(IPlayer attacker, Tile tile);
    void activateTiles();
    void freezeLostPlayer(IPlayer player);
    IPlayer whoseTurn();
}
