package com.github.dmitrykersh.bugs.api.board.validator;

import com.github.dmitrykersh.bugs.api.board.AbstractBoard;
import com.github.dmitrykersh.bugs.api.player.Player;
import com.github.dmitrykersh.bugs.api.board.tile.Tile;
import org.jetbrains.annotations.NotNull;

public interface TurnValidator {
    boolean validateTurn(final @NotNull AbstractBoard board, final @NotNull Player attacker, final @NotNull Tile attackedTile);
    boolean validateTurn(final @NotNull AbstractBoard board, final @NotNull Player attacker, final @NotNull Tile attackedTile, boolean reactivate);
}
