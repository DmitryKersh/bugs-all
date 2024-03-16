package com.github.dmitrykersh.bugs.engine.player;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.dmitrykersh.bugs.engine.board.AbstractBoard;
import javafx.scene.paint.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"board", "state"})
public class Player {
    private int queenTiles;
    @Getter
    private int turnsLeft;
    @Getter
    private String nickname;
    private int maxTurns;
    @Getter
    private Color color;
    @Getter
    private int rating;
    @Getter
    private String username;

    @Getter @Setter
    private AbstractBoard board;

    public Player(final @Nullable AbstractBoard board, final @NotNull String nickname, final @NotNull String username, int maxTurns, Color color, int rating) {
        this.color = color;
        this.nickname = nickname;
        this.maxTurns = maxTurns;
        this.turnsLeft = maxTurns;
        this.board = board;
        this.rating = rating;
        this.username = username;
    }

    public Player(final @Nullable AbstractBoard board, final @NotNull String username, int maxTurns, PlayerSettings playerSettings) {
        this.color = playerSettings.getColor();
        this.nickname = playerSettings.getNickname();
        this.maxTurns = maxTurns;
        this.turnsLeft = maxTurns;
        this.board = board;
        this.rating = playerSettings.getRating();
        this.username = username;
    }

    public Player(final @NotNull Color color, final @NotNull String nickname, final int turnsLeft){
        this.color = color;
        this.nickname = nickname;
        this.maxTurns = turnsLeft;
        this.turnsLeft = turnsLeft;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return nickname.equals(player.nickname) && color.equals(player.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickname, color);
    }

    public boolean tryMakeTurn(int tileId) {
        return board.tryMakeTurn(this, tileId);
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

    public void spendTurn() {
        if (turnsLeft > 0)
            turnsLeft--;
    }

    public void restoreTurns() {
        turnsLeft = maxTurns;
    }
}
