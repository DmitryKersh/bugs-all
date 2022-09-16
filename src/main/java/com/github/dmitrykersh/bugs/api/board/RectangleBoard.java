package com.github.dmitrykersh.bugs.api.board;

import com.github.dmitrykersh.bugs.api.board.layout.Layout;
import com.github.dmitrykersh.bugs.api.board.layout.PlayerConfig;
import com.github.dmitrykersh.bugs.api.board.layout.PlayerTemplate;
import com.github.dmitrykersh.bugs.api.board.layout.TileTemplate;
import com.github.dmitrykersh.bugs.api.board.validator.SimpleTurnValidator;
import com.github.dmitrykersh.bugs.api.board.validator.TurnValidator;
import com.github.dmitrykersh.bugs.api.player.HumanPlayer;
import com.github.dmitrykersh.bugs.api.player.Player;
import com.github.dmitrykersh.bugs.api.board.tile.Tile;
import com.github.dmitrykersh.bugs.api.board.tile.TileState;
import com.github.dmitrykersh.bugs.api.player.PlayerState;
import org.jetbrains.annotations.NotNull;

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
 * Row can be counted as (ID / colsAmount)
 * Column can be counted as (ID % colsAmount)
 * <p>
 * +---+---+---+---+---+
 * | 0 | 1 | 2 | 3 | 4 |
 * +---+---+---+---+---+
 * | 5 | 6 | 7 | 8 | 9 |
 * +---+---+---+---+---+
 */

public final class RectangleBoard implements Board {
    private final List<Player> players;
    private final List<Player> scoreboard;
    private final List<List<Tile>> tiles;
    private final TurnValidator turnValidator;

    // game-state variables
    private boolean ended;
    private int activePlayerNumber;

    // board size
    private final int rowsAmount;
    private final int colsAmount;

    private RectangleBoard(final @NotNull Layout layout, final @NotNull String configName, final @NotNull TurnValidator validator,
                           int rowsAmount, int colsAmount, final @NotNull List<String> nicknames) {
        this.rowsAmount = rowsAmount;
        this.colsAmount = colsAmount;
        this.turnValidator = validator;
        this.scoreboard = new LinkedList<>();

        activePlayerNumber = 0;
        ended = false;

        List<PlayerTemplate> templates = getPlayerTemplatesFromLayout(layout, configName);
        players = new ArrayList<>(templates.size());
        for (int i = 0; i < templates.size(); i++) {
            players.add(new HumanPlayer(this, nicknames.size() <= i ? ("p_" + i) : nicknames.get(i), new PlayerState(), templates.get(i).getMaxTurns()));
        }

        tiles = new ArrayList<>(rowsAmount);
        for (int row = 0; row < rowsAmount; row++) {
            tiles.add(row, new ArrayList<>(this.colsAmount));
            List<Tile> tileRow = tiles.get(row);

            for (int col = 0; col < this.colsAmount; col++) {
                tileRow.add(col, getTileFromLayout(layout, row * this.colsAmount + col));
            }
        }
    }

    public static RectangleBoard createBoard(final @NotNull Layout layout, final @NotNull String configName, final @NotNull TurnValidator validator,
                                             final int rowsAmount, final int colsAmount, final @NotNull List<String> nicknames) {
        if (rowsAmount <= 0 || colsAmount <= 0) {
            throw new IllegalArgumentException("Incorrect RectangleBoard size");
        }

        return new RectangleBoard(layout, configName, validator, rowsAmount, colsAmount, nicknames);
    }

    @Override
    public boolean ended() {
        return ended;
    }

    @Override
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    @Override
    public List<Player> getScoreboard() {
        return Collections.unmodifiableList(scoreboard);
    }

    @Override
    public void activateTiles(@NotNull Player player) {
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
    public boolean tryMakeTurn(@NotNull Player player, int tileId) {
        if (ended || players.get(activePlayerNumber) != player) return false;

        int row = tileId / colsAmount;
        int col = tileId % colsAmount;
        if (row >= rowsAmount) return false;

        Tile tile = tiles.get(row).get(col);
        if (turnValidator.validateTurn(this, player, tile)) {
            Player attackedPlayer = tile.getOwner();

            if (attackedPlayer != null && tile.getState() == QUEEN) {
                attackedPlayer.reduceQueenTile();
                if (!attackedPlayer.hasQueenTiles()) {
                    freezeLostPlayer(attackedPlayer);
                    if (players.size() == 1) {
                        freezeLostPlayer(players.get(0));
                        ended = true;
                    }
                }
            }

            tile.changeState(player);

            player.spendTurn();

            if (player.getTurnsLeft() == 0) {
                player.restoreTurns();
                activePlayerNumber++;
            }

            if (activePlayerNumber >= players.size()) activePlayerNumber = 0;
            return true;
        }
        return false;
    }

    @Override
    public Player getActivePlayer() {
        return players.get(activePlayerNumber);
    }

    @Override
    public void freezeLostPlayer(final @NotNull Player player) {
        players.remove(player);
        scoreboard.add(0, player);
    }

    private List<Tile> getNearbyTilesForPlayer(final @NotNull Tile origin, final @NotNull Player player) {
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

    private void activateTilesCluster(final @NotNull Tile origin, final @NotNull Player player) {
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
     * @return if not found in layout: default tile (id=id, owner=null, state=FREE, active=false)
     * if found: tile created from template
     */
    private Tile getTileFromLayout(final @NotNull Layout layout, int id) {
        // in layout file OwnerNumber-s start from 1. 0 represents null owner.
        TileTemplate tt = layout.getTileTemplate(id);
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

    private List<PlayerTemplate> getPlayerTemplatesFromLayout(final @NotNull Layout layout, String configName) {
        List<PlayerTemplate> playerInfo = new ArrayList<>();
        PlayerConfig config = layout.getPlayerConfigByName(configName);
        // TODO handle case if config==null
        if (config != null) {
            for (int i = 1; i <= config.getPlayerCount(); i++) {
                playerInfo.add(new PlayerTemplate(i, config.getMaxTurnsForPlayer(i)));
            }
        }
        return playerInfo;
    }

    ////////////// TESTING IN CONSOLE STUFF ////////////////////
    @Override
    public void print(final @NotNull PrintStream ps, final @NotNull Player player) {
        TurnValidator validator = SimpleTurnValidator.INSTANCE;
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
