package com.github.dmitrykersh.bugs.api.player;

import com.github.dmitrykersh.bugs.api.board.Board;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;

/**
 * Empty class for now. Don't use!
 */
public class BotPlayer implements Player {

    @Override
    public boolean tryMakeTurn(int tileId) {

        return false;
    }

    @Override
    public String getNickname() {
        return "BOT";
    }

    @Override
    public void setBoard(@NotNull Board b) {

    }

    @Override
    public boolean hasQueenTiles() {
        return false;
    }

    @Override
    public void reduceQueenTile() {

    }

    @Override
    public void restoreQueenTile() {

    }

    @Override
    public int getTurnsLeft() {
        return 0;
    }

    @Override
    public void spendTurn() {

    }

    @Override
    public void restoreTurns() {

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
