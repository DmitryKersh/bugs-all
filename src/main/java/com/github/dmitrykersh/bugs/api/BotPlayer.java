package com.github.dmitrykersh.bugs.api;

import javafx.scene.paint.Color;

public class BotPlayer implements IPlayer{

    @Override
    public int getRating() {
        return 0;
    }

    @Override
    public void setRating(int newRating) {

    }

    @Override
    public boolean hasQueenTiles() {
        return false;
    }

    @Override
    public void reduceQueenTile() {

    }

    @Override
    public int getTurnsLeft() {
        return 0;
    }

    @Override
    public void spendTurn() {

    }

    @Override
    public void restoreTurns(int numberOfTurns) {

    }

    @Override
    public void setPlace(int place) {

    }

    @Override
    public int getPlace() {
        return 0;
    }

    @Override
    public boolean getActive() {
        return false;
    }

    @Override
    public void setActive(boolean active) {

    }

    public Color getColor() {
        return null;
    }
}
