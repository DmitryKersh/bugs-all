package com.github.dmitrykersh.bugs.api;

import java.util.*;

/**
 * this class represents game board
 * it implements all game's rules, validates players' turns
 * it also makes a scoreboard
 */

public final class Board implements IBoard {
    private boolean ended;
    private final int turnsForPlayer;
    private final Vector<IPlayer> players;
    private final Vector<Vector<Tile>> tiles;
    private Integer currentPlaceForLostPlayer;

    private Board(Vector<Vector<Tile>> tiles, Vector<IPlayer> players, final int turns) {
        turnsForPlayer = turns;
        this.players = players;
        this.tiles = tiles;
        ended = false;
        currentPlaceForLostPlayer = players.size();
    }

    public static Board createBoard(int rows, int columns, Vector<IPlayer> players, final int turns) {
        Vector<Vector<Tile>> tiles = new Vector<>(rows);
        for (int row = 0; row < rows; row++) {
            tiles.set(row, new Vector<Tile>(columns));
            Vector<Tile> tileRow = tiles.get(row);
            for (int col = 0; col < columns; col++) {
                tileRow.set(col, new Tile(row * columns + col));
            }
        }

        return new Board(tiles, players, turns);
    }

    @Override
    public Vector<IPlayer> getPlayers() {
        return players;
    }

    @Override
    public void validateTurn(final IPlayer attacker, final Tile attackedTile) {
        if (attackedTile.getState() == TileState.WALL || attacker == attackedTile.getOwner()) return;
        for (Tile t : getNearbyTilesForPlayer(attackedTile, attacker))
            if (t.isActive()) {
                if (attackedTile.getState() == TileState.QUEEN) {
                    attackedTile.getOwner().reduceQueenTile();
                    if (!attackedTile.getOwner().hasQueenTiles())
                        freezeLostPlayer(attackedTile.getOwner());
                }
                attackedTile.changeState(attacker);
                attacker.spendTurn();
                return;
            }
    }

    @Override
    public void activateTiles() {
        for (Vector<Tile> row : tiles)
            for (Tile tile : row)
                tile.deactivate();

        for (Vector<Tile> row : tiles)
            for (Tile tile : row) {
                if (tile.isActive()) continue;
                if (tile.getState() == TileState.BUG)
                    activateTilesCluster(tile, tile.getOwner());
            }
    }

    private void activateTilesCluster(Tile origin, IPlayer owner /* to not call getOwner() every recursion step */) {
        if (origin.isActive()) return;
        origin.activate();
        for (Tile t : getNearbyTilesForPlayer(origin, owner))
            activateTilesCluster(t, owner);
    }

    @Override
    public void freezeLostPlayer(final IPlayer player) {
        player.setPlace(currentPlaceForLostPlayer);
        player.setActive(false);
        currentPlaceForLostPlayer--;
    }

    @Override
    public IPlayer whoseTurn() {
        return null;
    }

    private List<Tile> getNearbyTilesForPlayer(Tile origin, IPlayer player) {
        int x = origin.getId() % tiles.size();
        int y = origin.getId() / tiles.size();
        List<Tile> result = new LinkedList<>();

        result.add(tiles.get(y).get(x + 1));
        result.add(tiles.get(y).get(x - 1));
        result.add(tiles.get(y - 1).get(x));
        result.add(tiles.get(y + 1).get(x));

        result.removeIf(tile -> tile.getOwner() != player);

        return result;
    }
}
