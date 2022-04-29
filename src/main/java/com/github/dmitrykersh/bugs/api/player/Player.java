package com.github.dmitrykersh.bugs.api.player;

import com.github.dmitrykersh.bugs.api.board.Board;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Player {
    boolean tryMakeTurn(int tileId);
    String getNickname();
    void setBoard(final @NotNull Board b);

    void setPlace(int place);
    int getPlace();

    boolean getActive();
    void setActive(boolean active);

    boolean hasQueenTiles();
    void reduceQueenTile();
    void restoreQueenTile();

    int getTurnsLeft();
    void spendTurn();
    void restoreTurns();

    Color getColor();
}
