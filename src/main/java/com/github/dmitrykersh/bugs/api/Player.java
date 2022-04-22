package com.github.dmitrykersh.bugs.api;

import javafx.scene.paint.Color;

public interface Player {
    String getNickname();
    int getRating();
    void setRating(int newRating);

    void setPlace(int place);
    int getPlace();

    boolean getActive();
    void setActive(boolean active);

    boolean hasQueenTiles();
    void reduceQueenTile();

    int getTurnsLeft();
    void spendTurn();
    void restoreTurns(int numberOfTurns);

    Color getColor();
}
