package com.github.dmitrykersh.bugs.api.board.validator;

import com.github.dmitrykersh.bugs.api.board.tile.Tile;
import com.github.dmitrykersh.bugs.api.board.tile.TileState;
import com.github.dmitrykersh.bugs.api.board.Board;
import com.github.dmitrykersh.bugs.api.player.Player;

public class SimpleTurnValidator implements TurnValidator {
    public static TurnValidator INSTANCE = new SimpleTurnValidator();
    private SimpleTurnValidator() {}

    @Override
    public boolean validateTurn(final Board board, final Player attacker, final Tile attackedTile) {
        board.activateTiles(attacker);
        return (attackedTile.isActive()
                && attackedTile.getState() != TileState.WALL
                && attacker != attackedTile.getOwner()
                );
    }
}
