package com.github.dmitrykersh.bugs.engine.player;

import javafx.scene.paint.Color;
import lombok.*;

/**
 * This is pojo, list of which is returned by observer after end of game. It contains all data that is needed
 * for the UI to display all post-game info (rating change, etc...)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerResult {
    private int place;
    private int newRating;
    private int oldRating;
    private int ratingChange;
    private String username;
    private String nickname;
    private Color color;

    public static PlayerResult of(Player player) {
        return new PlayerResultBuilder()
                .color(player.getColor())
                .nickname(player.getNickname())
                .username(player.getUsername())
                .oldRating(player.getRating()).build();
    }
    public static PlayerResult of(Player player, int place) {
        return new PlayerResultBuilder()
                .color(player.getColor())
                .nickname(player.getNickname())
                .username(player.getUsername())
                .oldRating(player.getRating())
                .place(place).build();
    }
}
