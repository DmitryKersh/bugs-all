package com.github.dmitrykersh.bugs.api.board;

import com.github.dmitrykersh.bugs.api.player.HumanPlayer;
import com.github.dmitrykersh.bugs.api.player.Player;
import com.github.dmitrykersh.bugs.api.board.tile.Tile;
import com.github.dmitrykersh.bugs.api.board.tile.TileState;
import com.github.dmitrykersh.bugs.api.player.PlayerState;

import java.io.PrintStream;
import java.util.*;

/**
 * this class represents game board
 * it implements all game's rules, validates players' turns
 * it also makes a scoreboard after game
 */

public final class RectangleBoard implements Board {
    private boolean ended;
    private final List<Player> players;
    private final List<List<Tile>> tiles;
    private Integer currentPlaceForLostPlayer;

    private int rowsAmount;
    private int colsAmount;

    private RectangleBoard(List<List<Tile>> tiles, List<Player> players) {
        this.players = players;
        this.tiles = tiles;
        ended = false;
        currentPlaceForLostPlayer = players.size();
        rowsAmount = tiles.size();
        colsAmount = tiles.get(0) == null ? 0 : tiles.get(0).size();
    }

    public static RectangleBoard createBoard(Layout layout, int rowsAmount, int columnsAmount, List<String> nicknames) {
        List<List<Tile>> tiles = new ArrayList<>(rowsAmount);
        for (int row = 0; row < rowsAmount; row++) {
            tiles.add(row, new ArrayList<>(columnsAmount));
            List<Tile> tileRow = tiles.get(row);

            for (int col = 0; col < columnsAmount; col++) {
                tileRow.add(col, new Tile(row * columnsAmount + col));
            }
        }

        // Creating players by nicknames
        List<Player> players = new ArrayList<>(nicknames.size());
        for (String nickname : nicknames) {
            players.add(new HumanPlayer(nickname, new PlayerState(), 5 /* TODO: maxTurns should be passed by layout*/));
        }

        // TODO: Apply layout (tiles)

        tiles.get(0).get(0).setOwner(players.get(0));
        tiles.get(rowsAmount - 1).get(0).setOwner(players.get(1));

        return new RectangleBoard(tiles, players);
    }

    @Override
    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public void activateTiles() {
        for (List<Tile> row : tiles)
            for (Tile tile : row)
                tile.deactivate();

        for (List<Tile> row : tiles)
            for (Tile tile : row) {
                if (tile.isActive()) continue;
                if (tile.getState() == TileState.BUG)
                    activateTilesCluster(tile, tile.getOwner());
            }
    }

    private void activateTilesCluster(Tile origin, Player owner /* to not call getOwner() every recursion step */) {
        if (origin.isActive()) return;
        origin.activate();
        for (Tile t : getNearbyTilesForPlayer(origin, owner))
            activateTilesCluster(t, owner);
    }

    @Override
    public void freezeLostPlayer(final Player player) {
        player.setPlace(currentPlaceForLostPlayer);
        player.setActive(false);
        currentPlaceForLostPlayer--;
    }

    @Override
    public List<Tile> getNearbyTilesForPlayer(Tile origin, Player player) {
        int row = origin.getId() / tiles.size();
        int col = origin.getId() % tiles.size();
        List<Tile> result = new LinkedList<>();

        if (row < rowsAmount - 1)
            result.add(tiles.get(col).get(row + 1));
        if (row > 0)
            result.add(tiles.get(col).get(row - 1));
        if (col > 0)
            result.add(tiles.get(col - 1).get(row));
        if (col < colsAmount - 1)
            result.add(tiles.get(col + 1).get(row));

        result.removeIf(tile -> tile.getOwner() != player);

        return result;
    }

    ////////////// TESTING IN CONSOLE STUFF ////////////////////
    @Override
    public void print(PrintStream ps) {
        for (List<Tile> row : tiles) {
            for (Tile t : row) {
                ps.print(tileInfo(t));
            }
            ps.print("\n");
        }
    }

    private String tileInfo(Tile tile) {
        StringBuilder str = new StringBuilder("[ ");
        str.append(tile.isActive() ? "A" : "D")
                .append("; id=").append(tile.getId())
                .append("; owner=").append(tile.getOwner() == null ? "null" : tile.getOwner().getNickname())
                .append("; state=").append(tile.getState().toString())
                .append(" ] ");
        return str.toString();
    }
    ////////////////////////////////////////////////////////////
}
