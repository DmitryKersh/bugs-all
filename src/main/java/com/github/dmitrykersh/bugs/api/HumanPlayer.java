package com.github.dmitrykersh.bugs.api;

import javafx.scene.paint.Color;

public class HumanPlayer implements IPlayer{
    private int rating;
    private boolean hasQueenTiles;
    private int turnsLeft;

    @Override
    public int getRating() {
        return rating;
    }

    @Override
    public void setRating(final int newRating) {
        rating = newRating;
    }

    @Override
    public boolean hasQueenTiles() {
        return hasQueenTiles;
    }

    @Override
    public int getTurnsLeft() {
        return turnsLeft;
    }

    @Override
    public Color getColor() {
        return null;
    }
}
