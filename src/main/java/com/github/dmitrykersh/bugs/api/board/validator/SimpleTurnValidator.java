package com.github.dmitrykersh.bugs.api.board.validator;

import com.github.dmitrykersh.bugs.api.board.AbstractBoard;
import com.github.dmitrykersh.bugs.api.board.tile.Tile;
import com.github.dmitrykersh.bugs.api.board.tile.TileState;
import com.github.dmitrykersh.bugs.api.player.Player;
import org.jetbrains.annotations.NotNull;

public class SimpleTurnValidator implements TurnValidator {
    public static TurnValidator INSTANCE = new SimpleTurnValidator();
    private SimpleTurnValidator() {}

    @Override
    public boolean validateTurn(final @NotNull AbstractBoard board, final @NotNull Player attacker, final @NotNull Tile attackedTile) {
        return validateTurn(board, attacker, attackedTile, true);
    }

    @Override
    public boolean validateTurn(@NotNull AbstractBoard board, @NotNull Player attacker, @NotNull Tile attackedTile, boolean reactivate) {
        if (reactivate)
            board.activateTiles(attacker);
        return (attackedTile.isActive()
                && attackedTile.getState() != TileState.WALL
                && attacker != attackedTile.getOwner()
        );
    }

}
