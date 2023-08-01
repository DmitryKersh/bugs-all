package com.github.dmitrykersh.bugs.logic.board.validator;

import com.github.dmitrykersh.bugs.logic.board.AbstractBoard;
import com.github.dmitrykersh.bugs.logic.player.Player;
import com.github.dmitrykersh.bugs.logic.board.tile.Tile;
import org.jetbrains.annotations.NotNull;

public interface TurnValidator {
    boolean validateTurn(final @NotNull AbstractBoard board, final @NotNull Player attacker, final @NotNull Tile attackedTile);
    boolean validateTurn(final @NotNull AbstractBoard board, final @NotNull Player attacker, final @NotNull Tile attackedTile, boolean reactivate);
}
