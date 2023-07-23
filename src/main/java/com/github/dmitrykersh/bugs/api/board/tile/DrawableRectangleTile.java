package com.github.dmitrykersh.bugs.api.board.tile;

import javafx.scene.shape.Rectangle;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class DrawableRectangleTile extends Rectangle {
    @Getter
    private final Tile tile;

}
