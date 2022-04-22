package com.github.dmitrykersh.bugs.api.board.validator;

import com.github.dmitrykersh.bugs.api.board.tile.Tile;
import com.github.dmitrykersh.bugs.api.board.tile.TileState;
import com.github.dmitrykersh.bugs.api.board.Board;
import com.github.dmitrykersh.bugs.api.player.Player;

public class SimpleTurnValidator implements TurnValidator {
    @Override
    public boolean validateTurn(final Board board, final Player attacker, final Tile attackedTile) {
        if (attackedTile.getState() == TileState.WALL || attacker == attackedTile.getOwner()) return false;
        for (Tile t : board.getNearbyTilesForPlayer(attackedTile, attacker))
            if (t.isActive()) {
                /*
                //////// This should be left to board /////////

                if (attackedTile.getState() == TileState.QUEEN) {
                    attackedTile.getOwner().reduceQueenTile();
                    if (!attackedTile.getOwner().hasQueenTiles())
                        board.freezeLostPlayer(attackedTile.getOwner());
                }
                */
                attackedTile.changeState(attacker);
                attacker.spendTurn();
                return true;
            }
        return false;
    }
}
