package com.github.dmitrykersh.bugs.api;

public class SimpleTurnValidator implements TurnValidator {
    @Override
    public void validateTurn(final Board board, final Player attacker, final Tile attackedTile) {
        if (attackedTile.getState() == TileState.WALL || attacker == attackedTile.getOwner()) return;
        for (Tile t : board.getNearbyTilesForPlayer(attackedTile, attacker))
            if (t.isActive()) {
                if (attackedTile.getState() == TileState.QUEEN) {
                    attackedTile.getOwner().reduceQueenTile();
                    if (!attackedTile.getOwner().hasQueenTiles())
                        board.freezeLostPlayer(attackedTile.getOwner());
                }
                attackedTile.changeState(attacker);
                attacker.spendTurn();
                return;
            }
    }
}
