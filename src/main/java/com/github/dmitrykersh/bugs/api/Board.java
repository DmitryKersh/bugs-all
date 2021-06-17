package com.github.dmitrykersh.bugs.api;

import java.util.*;

public final class Board implements IBoard{
    private boolean ended;
    private final int turnsForPlayer;
    private final Map<IPlayer, Integer> scoreboard;
    private final Vector<Vector<Tile>> tiles;

    private Board(Vector<Vector<Tile>> tiles, Map<IPlayer, Integer> scoreboard, final int turns) {
        turnsForPlayer = turns;
        this.scoreboard = scoreboard;
        this.tiles = tiles;
        ended = false;
    }

    public static Board createBoard(int rows, int columns, Vector<IPlayer> players, final int turns) {
        Map<IPlayer, Integer> scoreboard = new HashMap<>();
        for (IPlayer player : players) scoreboard.put(player, 0);

        Vector<Vector<Tile>> tiles = new Vector<>(rows);
        for (Vector<Tile> row : tiles) row = new Vector<>(columns);

        return new Board(tiles, scoreboard, turns);
    }

    @Override
    public void validateAttack(final IPlayer attacker, final Tile tile) {
        if (tile.getState() == TileState.WALL || attacker == tile.getOwner()) return;
        for (Tile t : getNearbyTilesForPlayer(attacker))
            if (t.isActive()) {
                tile.changeState(attacker);
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
        for (Tile t : getNearbyTilesForPlayer(owner))
            activateTilesCluster(t, owner);
    }

    @Override
    public void freezeLostPlayer(final IPlayer player) {

    }

    @Override
    public IPlayer whoseTurn() {
        return null;
    }

    private List<Tile> getNearbyTilesForPlayer(IPlayer player){
        return new LinkedList<>();
    }
}
