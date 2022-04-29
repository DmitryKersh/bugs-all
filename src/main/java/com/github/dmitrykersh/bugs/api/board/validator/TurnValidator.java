package com.github.dmitrykersh.bugs.api.board.validator;

import com.github.dmitrykersh.bugs.api.player.Player;
import com.github.dmitrykersh.bugs.api.board.tile.Tile;
import com.github.dmitrykersh.bugs.api.board.Board;
import org.jetbrains.annotations.NotNull;

public interface TurnValidator {
    boolean validateTurn(final @NotNull Board board, final @NotNull Player attacker, final @NotNull Tile attackedTile);
}
