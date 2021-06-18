package com.github.dmitrykersh.bugs.api;

import javafx.scene.paint.Color;

public interface IPlayer {
    int getRating();
    void setRating(int newRating);

    boolean hasQueenTiles();
    void reduceQueenTile();

    int getTurnsLeft();
    void spendTurn();
    void restoreTurns(int numberOfTurns);

    void setPlace(int place);
    int getPlace();

    boolean getActive();
    void setActive(boolean active);

    Color getColor();
}
