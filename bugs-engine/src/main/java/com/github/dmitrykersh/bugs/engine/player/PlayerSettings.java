package com.github.dmitrykersh.bugs.engine.player;

import javafx.scene.paint.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class PlayerSettings {
    @Getter
    private final String nickname;
    @Getter
    private final Color color;
}
