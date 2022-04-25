package com.github.dmitrykersh.bugs.api.board.validator;

import com.github.dmitrykersh.bugs.api.player.Player;
import com.github.dmitrykersh.bugs.api.board.tile.Tile;
import com.github.dmitrykersh.bugs.api.board.Board;

public interface TurnValidator {
    boolean validateTurn(Board board, Player attacker, Tile attackedTile);
}
