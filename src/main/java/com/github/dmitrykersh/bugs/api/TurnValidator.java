package com.github.dmitrykersh.bugs.api;

public interface TurnValidator {
    void validateTurn(Board board, Player attacker, Tile tile);
}
