package com.github.dmitrykersh.bugs.logic.player;

import com.github.dmitrykersh.bugs.logic.board.AbstractBoard;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;


public interface Player {
    boolean tryMakeTurn(int tileId);
    String getNickname();
    void setBoard(final @NotNull AbstractBoard b);
    AbstractBoard getBoard();

    boolean hasQueenTiles();
    void reduceQueenTile();
    void restoreQueenTile();

    int getTurnsLeft();
    void spendTurn();
    void restoreTurns();

    Color getColor();
}
