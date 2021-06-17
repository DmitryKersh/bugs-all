package com.github.dmitrykersh.bugs.api;

import javafx.scene.paint.Color;

public class BotPlayer implements IPlayer{
    @Override
    public int getRating() {
        return 0;
    }

    @Override
    public void setRating(final int newRating) {

    }

    @Override
    public boolean hasQueenTiles() {
        return false;
    }

    @Override
    public int getTurnsLeft() {
        return 0;
    }

    @Override
    public Color getColor() {
        return null;
    }
}
