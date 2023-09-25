package com.github.dmitrykersh.bugs.gui.viewer;

import com.github.dmitrykersh.bugs.engine.TextureCollection;
import com.github.dmitrykersh.bugs.engine.board.BoardDto;
import com.github.dmitrykersh.bugs.engine.board.tile.DrawableRectangleTile;
import com.github.dmitrykersh.bugs.engine.board.tile.Tile;
import com.github.dmitrykersh.bugs.engine.player.Player;
import com.github.dmitrykersh.bugs.engine.util.TextureUtils;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.effect.Blend;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import lombok.val;

import static com.github.dmitrykersh.bugs.engine.board.BoardState.ENDED;
import static com.github.dmitrykersh.bugs.engine.board.tile.TileState.UNAVAILABLE;

public class RectangleBoardViewer extends BoardViewer {
    private static final String QUEEN_TEX_NAME = "queen";
    private static final String WALL_TEX_NAME = "wall";
    private static final String EMPTY_TEX_NAME = "empty";
    private static final String BUG_TEX_NAME = "bug";
    private static final String UNAVAILABLE_TEX_NAME = "unavailable";
    private final int size_x;
    private final int size_y;

    public RectangleBoardViewer(BoardDto dto, EventHandler<MouseEvent> clickOnTileEvent) {
        super(dto, clickOnTileEvent);
        size_x = params.get("size_x");
        size_y = params.get("size_y");
    }

    @Override
    public void buildDrawableGrid() {
        grid.getChildren().clear();
        for (int i = 0; i < size_y; i++) {
            for (int j = 0; j < size_x; j++) {
                DrawableRectangleTile rect = createDrawableTile(j, i, 50);
                rect.setOnMouseClicked(clickOnTileEvent);
                grid.getChildren().add(rect);
            }
        }
    }

    @Override
    protected void redrawTile(int id) {
        for (val dt : grid.getChildren()) {
            if (dt instanceof DrawableRectangleTile && ((DrawableRectangleTile) dt).getTile().getId() == id)
                redrawTile((DrawableRectangleTile) dt);
        }
    }

    private DrawableRectangleTile createDrawableTile(int x, int y, int size) {
        Tile t = tiles.get(y * size_x + x);
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
}
