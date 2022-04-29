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
 *
 * @TileID from zero, right-to-left, top-to-bottom.
 * Thus, ID can be counted as (row * colsAmount + col)
 *       Row can be counted as (ID / colsAmount)
 *       Column can be counted as (ID % colsAmount)
 *
 *  +---+---+---+---+---+
 *  | 0 | 1 | 2 | 3 | 4 |
 *  +---+---+---+---+---+
 *  | 5 | 6 | 7 | 8 | 9 |
 *  +---+---+---+---+---+
 */

public final class RectangleBoard implements Board {
    private boolean ended;
    private final List<Player> players;
    private final List<List<Tile>> tiles;
    private Integer currentPlaceForLostPlayer;
    private int activePlayerNumber;
    private TurnValidator turnValidator;

    private int rowsAmount;
    private int colsAmount;

    private RectangleBoard(Layout layout, TurnValidator validator, int rowsAmount, int colsAmount, List<Player> players) {
        this.rowsAmount = rowsAmount;
        this.colsAmount = colsAmount;
        this.turnValidator = validator;
        this.players = players;
        activePlayerNumber = 0;

        ended = false;
        currentPlaceForLostPlayer = players.size();
        for (Player p : players)
            p.setBoard(this);

        tiles = new ArrayList<>(rowsAmount);
        for (int row = 0; row < rowsAmount; row++) {
            tiles.add(row, new ArrayList<>(this.colsAmount));
            List<Tile> tileRow = tiles.get(row);

            for (int col = 0; col < this.colsAmount; col++) {
                tileRow.add(col, getFromLayout(layout,row * this.colsAmount + col));
            }
        }
    }

    public static RectangleBoard createBoard(Layout layout, TurnValidator validator, int rowsAmount, int colsAmount, List<String> nicknames) {
        if (rowsAmount <= 0 || colsAmount <= 0){
            throw new IllegalArgumentException("Incorrect RectangleBoard size");
        }

        // Creating players by nicknames
        List<Player> players = new ArrayList<>(nicknames.size());
        for (String nickname : nicknames) {
            players.add(new HumanPlayer(null, nickname, new PlayerState(), 5 /* TODO: maxTurns should be passed by layout*/));
        }

        return new RectangleBoard(layout, validator, rowsAmount, colsAmount, players);
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

    @Override
    public boolean tryMakeTurn(Player player, int tileId) {
        int row = tileId / colsAmount;
        int col = tileId % colsAmount;
        if (row >= rowsAmount) return false;

        Tile tile = tiles.get(row).get(col);
        if (turnValidator.validateTurn(this, player, tile)) {
            Player attackedPlayer = tile.getOwner();

            if (attackedPlayer != null && tile.getState() == QUEEN) {
                attackedPlayer.reduceQueenTile();
                if (!attackedPlayer.hasQueenTiles())
                    freezeLostPlayer(attackedPlayer);
            }

            tile.changeState(player);
            activePlayerNumber++;
            if (activePlayerNumber >= players.size()) activePlayerNumber = 0;
            return true;
        }
        return false;
    }

    @Override
    public void freezeLostPlayer(final Player player) {
        player.setPlace(currentPlaceForLostPlayer);
        players.remove(player);
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

    @Override
    public Player getActivePlayer() {
        return players.get(activePlayerNumber);
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

    /**
     * This method gets TileTemplate from a given layout and returns a Tile created from it.
     * In addition, it counts QUEEN-tiles: if there's a QUEEN tile,
     * the method increments PlayerState.queenTiles for its owner
     *
     * @param layout given layout
     * @param id     tile id to search for in layout
     * @return       if not found in layout: default tile (id=id, owner=null, state=FREE, active=false)
     *               if found: tile created from template
     */
    private Tile getFromLayout(Layout layout, int id) {
        // in layout file OwnerNumber-s start from 1. 0 represents null owner.
        Layout.TileTemplate tt = layout.getTileTemplate(id);
        int ownerNumber;

        if (tt == null || players.size() <= (ownerNumber = tt.getOwnerNumber()) - 1) {
            return new Tile(id);
        }

        TileState state = tt.getState();
        if (state == QUEEN && ownerNumber != 0) {
            players.get(ownerNumber - 1).restoreQueenTile();
        }

        return new Tile(
                id,
                ownerNumber == 0 ? null : players.get(ownerNumber - 1),
                tt.getState()
        );
    }

    ////////////// TESTING IN CONSOLE STUFF ////////////////////
    @Override
    public void print(PrintStream ps, Player player) {
        SimpleTurnValidator validator = new SimpleTurnValidator();
        for (List<Tile> row : tiles) {
            for (Tile t : row) {
                ps.printf("[ %3s  %1s %5.5s %5.5s]",
                        t.getId(),
                        validator.validateTurn(this, player, t) ? "+" : " ",
                        t.getOwner() == null ? "-" : t.getOwner().getNickname(),
                        t.getState() == FREE ? "-" : t.getState().toString());
            }
            ps.print("\n");
        }
    }

    ////////////////////////////////////////////////////////////
}
