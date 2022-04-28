package com.github.dmitrykersh.bugs.api.board;

import com.github.dmitrykersh.bugs.api.board.validator.SimpleTurnValidator;
import com.github.dmitrykersh.bugs.api.board.validator.TurnValidator;
import com.github.dmitrykersh.bugs.api.player.HumanPlayer;
import com.github.dmitrykersh.bugs.api.player.Player;
import com.github.dmitrykersh.bugs.api.board.tile.Tile;
import com.github.dmitrykersh.bugs.api.board.tile.TileState;
import com.github.dmitrykersh.bugs.api.player.PlayerState;

import static com.github.dmitrykersh.bugs.api.board.tile.TileState.*;

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
    private TurnValidator turnValidator;

    private int rowsAmount;
    private int colsAmount;

    private RectangleBoard(TurnValidator validator, List<List<Tile>> tiles, List<Player> players) {
        this.players = players;
        for (Player p : players)
            p.setBoard(this);
        this.tiles = tiles;
        ended = false;
        currentPlaceForLostPlayer = players.size();
        rowsAmount = tiles.size();
        colsAmount = tiles.get(0).size();
        this.turnValidator = validator;
    }

    public static RectangleBoard createBoard(Layout layout, TurnValidator validator, int rowsAmount, int columnsAmount, List<String> nicknames) {
        if (rowsAmount <= 0 || columnsAmount <= 0){
            throw new IllegalArgumentException("Incorrect RectangleBoard size");
        }

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
            players.add(new HumanPlayer(null, nickname, new PlayerState(), 5 /* TODO: maxTurns should be passed by layout*/));
        }

        // TODO: Apply layout (tiles)

        /////// INITIAL STATE (WILL BE CREATED BY LAYOUT LATER) ///////
        Player p1 = players.get(0);
        Player p2 = players.get(1);

        tiles.get(0).get(0).changeState(p1);
        tiles.get(7).get(3).changeState(p2);
/*
        tiles.get(1).get(0).changeState(p1);
        tiles.get(1).get(1).changeState(p1);
        tiles.get(1).get(2).changeState(p1);
        tiles.get(1).get(3).changeState(p1);


        tiles.get(1).get(0).changeState(p2);
        tiles.get(1).get(1).changeState(p2);
        tiles.get(1).get(2).changeState(p2);

        tiles.get(5).get(0).changeState(p2);
        tiles.get(2).get(0).changeState(p2);

        tiles.get(6).get(3).changeState(p2);
        tiles.get(7).get(3).changeState(p2);

        tiles.get(6).get(3).changeState(p1);
        tiles.get(7).get(3).changeState(p1);

        tiles.get(7).get(2).changeState(p1);*/
        ///////////////////////////////////////////////////////////////

        return new RectangleBoard(validator, tiles, players);
    }

    @Override
    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public void activateTiles(Player player) {
        for (List<Tile> row : tiles)
            for (Tile tile : row)
                tile.deactivate();

        for (List<Tile> row : tiles)
            for (Tile tile : row) {
                if (tile.getOwner() != player) continue;
                // bug or queen activates tiles around no matter what
                if (tile.getState() == BUG
                        || tile.getState() == QUEEN
                        || (tile.getState() == WALL && tile.isActive()))
                    activateTilesCluster(tile, player);
            }
    }

    private void activateTilesCluster(Tile origin, Player player) {
        origin.activate();
        for (Tile tile : getNearbyTilesForPlayer(origin, player)) {
            if (tile.getState() == UNAVAILABLE) return;
            if (tile.getOwner() == player && tile.getState() == WALL && !tile.isActive())
                activateTilesCluster(tile, player);
            else if (tile.getState() == FREE ||
                    (tile.getOwner() != player && (tile.getState() == BUG
                                                || tile.getState() == QUEEN)))
                tile.activate();
        }
    }
    @Override
    public boolean tryMakeTurn(Player player, int tileId) {
        int row = tileId / colsAmount;
        int col = tileId % colsAmount;
        if (row >= rowsAmount) return false;

        Tile tile = tiles.get(row).get(col);
        if (turnValidator.validateTurn(this, player, tile)) {
            tile.changeState(player);
            return true;
        }
        return false;
    }

    @Override
    public void freezeLostPlayer(final Player player) {
        player.setPlace(currentPlaceForLostPlayer);
        player.setActive(false);
        currentPlaceForLostPlayer--;
    }

    @Override
    public List<Tile> getNearbyTilesForPlayer(Tile origin, Player player) {
        int row = origin.getId() / colsAmount;
        int col = origin.getId() % colsAmount;
        List<Tile> result = new LinkedList<>();

        if (row < rowsAmount - 1)
            result.add(tiles.get(row + 1).get(col));
        if (row > 0)
            result.add(tiles.get(row - 1).get(col));
        if (col > 0)
            result.add(tiles.get(row).get(col - 1));
        if (col < colsAmount - 1)
            result.add(tiles.get(row).get(col + 1));

        return result;
    }

    ////////////// TESTING IN CONSOLE STUFF ////////////////////
    @Override
    public void print(PrintStream ps, Player player) {
        for (List<Tile> row : tiles) {
            for (Tile t : row) {
                ps.print(tileInfo(t, player));
            }
            ps.print("\n");
        }
    }

    private String tileInfo(Tile tile, Player player) {
        SimpleTurnValidator validator = new SimpleTurnValidator();
        StringBuilder str = new StringBuilder("[ ");
        str.append(tile.getId())
                .append(" ").append(validator.validateTurn(this, player, tile) ? "+" : " ")
                .append(" ").append(tile.getOwner() == null ? "-" : tile.getOwner().getNickname())
                .append(" ").append(tile.getState() == FREE ? " - " : tile.getState().toString())
                .append(" ] ");
        return str.toString();
    }
    ////////////////////////////////////////////////////////////
}
