package com.github.dmitrykersh.bugs.engine.player;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.dmitrykersh.bugs.engine.board.AbstractBoard;
import javafx.scene.paint.Color;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"board", "state"})
public class Player {
    private int queenTiles;
    private int turnsLeft;
    private String nickname;
    private int maxTurns;
    private Color color;

    private AbstractBoard board;

    public Player(final @Nullable AbstractBoard board, final @NotNull String nickname, int maxTurns, Color color) {
        this.color = color;
        this.nickname = nickname;
        this.maxTurns = maxTurns;
        this.board = board;
    }

    public boolean tryMakeTurn(int tileId) {
        return board.tryMakeTurn(this, tileId);
    }

    public String getNickname() {
        return nickname;
    }

    public void setBoard(final @NotNull AbstractBoard b) {
        board = b;
    }

    public AbstractBoard getBoard() {
        return board;
    }

    public boolean hasQueenTiles() {
        return queenTiles > 0;
    }

    public void reduceQueenTile() {
        queenTiles--;
    }

    public void restoreQueenTile() {
        queenTiles++;
    }

    public int getTurnsLeft() {
        return turnsLeft;
    }

    public void spendTurn() {
        if (turnsLeft > 0)
            turnsLeft--;
    }

    public void restoreTurns() {
        turnsLeft = maxTurns;
    }

    public Color getColor() {
        return color;
    }
}
