package com.github.dmitrykersh.bugs.api.player;

import javafx.scene.paint.Color;

public interface Player {
    String getNickname();

    void setPlace(int place);
    int getPlace();

    boolean getActive();
    void setActive(boolean active);

    boolean hasQueenTiles();
    void reduceQueenTile();

    int getTurnsLeft();
    void spendTurn();
    void restoreTurns();

    Color getColor();
}
