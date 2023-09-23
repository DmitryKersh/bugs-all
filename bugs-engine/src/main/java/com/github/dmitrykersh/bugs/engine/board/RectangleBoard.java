package com.github.dmitrykersh.bugs.engine.board;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dmitrykersh.bugs.engine.board.layout.Layout;
import com.github.dmitrykersh.bugs.engine.board.tile.DrawableRectangleTile;
import com.github.dmitrykersh.bugs.engine.board.validator.SimpleTurnValidator;
import com.github.dmitrykersh.bugs.engine.board.validator.TurnValidator;
import com.github.dmitrykersh.bugs.engine.player.Player;
import com.github.dmitrykersh.bugs.engine.board.tile.Tile;
import com.github.dmitrykersh.bugs.engine.player.PlayerSettings;
import com.github.dmitrykersh.bugs.engine.TextureCollection;
import com.github.dmitrykersh.bugs.engine.util.TextureUtils;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.effect.Blend;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import static com.github.dmitrykersh.bugs.engine.board.BoardState.*;
import static com.github.dmitrykersh.bugs.engine.board.tile.TileState.*;

import java.io.PrintStream;
import java.util.*;
import java.util.List;

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

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public final class RectangleBoard extends AbstractBoard {
    private static final int TILE_SIZE = 50;

    private final List<List<Tile>> tiles;

    private static final String QUEEN_TEX_NAME = "queen";
    private static final String WALL_TEX_NAME = "wall";
    private static final String EMPTY_TEX_NAME = "empty";
    private static final String BUG_TEX_NAME = "bug";
    private static final String UNAVAILABLE_TEX_NAME = "unavailable";

    // board size
    private final int rowsAmount;
    private final int colsAmount;

    private RectangleBoard(final @NotNull Layout layout, final @NotNull String gameModeName, final @NotNull TurnValidator validator,
                           int rowsAmount, int colsAmount, final @NotNull List<PlayerSettings> playerSettings) {
        super(layout, gameModeName, validator);
        this.rowsAmount = rowsAmount;
        this.colsAmount = colsAmount;


        for (int i = 0; i < playerTemplates.size(); i++) {
            players.set(i, new Player(this,
                    playerSettings.size() <= i ? ("p_" + i) : playerSettings.get(i).getNickname(),
                    playerTemplates.get(i).getMaxTurns(),
                    playerSettings.get(i).getColor()));
        }

        tiles = new ArrayList<>(rowsAmount);
        prepareBoard();

    }

    private RectangleBoard(final @NotNull Layout layout, final @NotNull String gameModeName, final @NotNull TurnValidator validator,
                           int rowsAmount, int colsAmount) {
        super(layout, gameModeName, validator);
        this.rowsAmount = rowsAmount;
        this.colsAmount = colsAmount;

        tiles = new ArrayList<>(rowsAmount);
    }

    public static RectangleBoard createBoardWithPlayers(final @NotNull Layout layout, final @NotNull String gameModeName, final @NotNull TurnValidator validator,
                                                        final @NotNull List<PlayerSettings> playerSettings) {
        int rowsAmount = layout.getParam("size_y");
        int colsAmount = layout.getParam("size_x");
        if (rowsAmount <= 0 || colsAmount <= 0) {
            throw new IllegalArgumentException("Incorrect RectangleBoard size");
        }
        return new RectangleBoard(layout, gameModeName, validator, rowsAmount, colsAmount, playerSettings);
    }

    public static RectangleBoard createEmptyBoard(final @NotNull Layout layout, final @NotNull String gameModeName, final @NotNull TurnValidator validator) {
        int rowsAmount = layout.getParam("size_y");
        int colsAmount = layout.getParam("size_x");
        if (rowsAmount <= 0 || colsAmount <= 0) {
            throw new IllegalArgumentException("Incorrect RectangleBoard size");
        }
        return new RectangleBoard(layout, gameModeName, validator, rowsAmount, colsAmount);
    }

    public boolean prepareBoard() {
        for (Player p : players)
            if (p == null)
                return false;

        layout.processTiles();
        for (int row = 0; row < rowsAmount; row++) {
            tiles.add(row, new ArrayList<>(this.colsAmount));
            List<Tile> tileRow = tiles.get(row);

            for (int col = 0; col < this.colsAmount; col++) {
                tileRow.add(col, createTileFromLayout(row * this.colsAmount + col));
            }
        }
        if (checkIfStalemate(getActivePlayer())) {
            System.out.println("Impossible setup. First player has no legal moves");
            return false;
        }

        state = PREPARED;
        return true;
    }

    @Override
    protected Tile getTileById(int tileId) throws RuntimeException {
        int row = tileId / colsAmount;
        int col = tileId % colsAmount;
        if (row >= rowsAmount) {
            throw new RuntimeException(String.format("RectangleBoard: no tile with id = %d : has %d rows and %d columns", tileId, rowsAmount, colsAmount));
        }

        return tiles.get(row).get(col);
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
    protected List<Tile> getNearbyTilesForPlayer(final @NotNull Tile origin, final @NotNull Player player) {
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
    protected boolean checkIfStalemate(Player activePlayer) {
        activateTiles(activePlayer);
        for (val row : tiles)
            for (val tile : row)
                if (turnValidator.validateTurn(this, activePlayer, tile, false))
                    return false;
        return true;
    }

    @Override
    public String getTilesAsJsonArray() throws JsonProcessingException {
        List<Tile> tileList = new ArrayList<>();
        for (val row : tiles)
            tileList.addAll(row);
        return new ObjectMapper().writer().writeValueAsString(tileList);
    }

    @Override
    public Group buildDrawableGrid() {
        Group grid = new Group();
        for (int i = 0; i < rowsAmount; i++) {
            for (int j = 0; j < colsAmount; j++) {
                DrawableRectangleTile rect = createDrawableTile(j, i, TILE_SIZE);
                rect.setOnMouseClicked(clickOnTileEvent());
                grid.getChildren().add(rect);
            }
        }
        return grid;
    }

    @Override
    public BoardDto makeDto() {
        // todo: cache it
        List<Tile> tileList = new ArrayList<>();
        for (val row : tiles)
            tileList.addAll(row);
        return BoardDto.builder()
                .boardType("RECT")
                .players(getPlayers())
                .tiles(tileList)
                .params(Map.of(
                        "size_x", colsAmount,
                        "size_y", rowsAmount)
                ).build();
    }

    private void activateTilesCluster(final @NotNull Tile origin, final @NotNull Player player) {
        origin.activate();
        for (Tile tile : getNearbyTilesForPlayer(origin, player)) {
            if (tile.getOwner() == player && tile.getState() == WALL && !tile.isActive())
                activateTilesCluster(tile, player);
            else if (tile.getState() == FREE ||
                    (tile.getOwner() != player && (tile.getState() == BUG
                            || tile.getState() == QUEEN)))
                tile.activate();
        }
    }

    private EventHandler<MouseEvent> clickOnTileEvent() {
        return event -> {
            if (state == ENDED) return;

            DrawableRectangleTile tile = (DrawableRectangleTile) event.getTarget();

            Player activePlayer = this.getActivePlayer();
            if (activePlayer.tryMakeTurn(tile.getTile().getId())) {
                redrawTile(tile);
            }
        };
    }

    private void redrawTile(DrawableRectangleTile rt) {
        Tile t = rt.getTile();
        String texName;
        switch (t.getState()) {
            case WALL -> texName = WALL_TEX_NAME;
            case QUEEN -> texName = QUEEN_TEX_NAME;
            case BUG -> texName = BUG_TEX_NAME;
            case FREE -> texName = EMPTY_TEX_NAME;
            case UNAVAILABLE -> texName = UNAVAILABLE_TEX_NAME;
            default -> texName = EMPTY_TEX_NAME;
        }
        Image tex = TextureCollection.getImageByName(texName);
        rt.setFill(new ImagePattern(tex));
        if (t.getOwner() != null) {
            Blend b = TextureUtils.makeBlend(tex, t.getOwner().getColor(), rt.getX(), rt.getY(), rt.getWidth(), rt.getHeight());
            rt.setEffect(b);
        }
    }

    private DrawableRectangleTile createDrawableTile(int x, int y, int size) {
        Tile t = tiles.get(y).get(x);
        DrawableRectangleTile drawableRectangleTile = new DrawableRectangleTile(t);
        drawableRectangleTile.setX(x * size);
        drawableRectangleTile.setY(y * size);
        drawableRectangleTile.setHeight(size);
        drawableRectangleTile.setWidth(size);

        redrawTile(drawableRectangleTile);
        if (t.getState() != UNAVAILABLE)
            drawableRectangleTile.setStroke(Color.BLACK);

        return drawableRectangleTile;
    }

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
}
