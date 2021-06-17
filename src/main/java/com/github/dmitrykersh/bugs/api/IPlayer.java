package com.github.dmitrykersh.bugs.api;

import javafx.scene.paint.Color;

public interface IPlayer {
    int getRating();

    void setRating(int newRating);

    boolean hasQueenTiles();

    int getTurnsLeft();

    Color getColor();
}
