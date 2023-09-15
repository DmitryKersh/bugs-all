package com.github.dmitrykersh.bugs.engine.board.validator;

import com.github.dmitrykersh.bugs.engine.board.AbstractBoard;
import com.github.dmitrykersh.bugs.engine.player.Player;
import com.github.dmitrykersh.bugs.engine.board.tile.Tile;
import org.jetbrains.annotations.NotNull;

public interface TurnValidator {
    boolean validateTurn(final @NotNull AbstractBoard board, final @NotNull Player attacker, final @NotNull Tile attackedTile);
    boolean validateTurn(final @NotNull AbstractBoard board, final @NotNull Player attacker, final @NotNull Tile attackedTile, boolean reactivate);
}
