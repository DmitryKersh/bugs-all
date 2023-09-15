package com.github.dmitrykersh.bugs.engine.board.layout;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class PlayerTemplate {
    @Getter
    private final int number;
    @Getter
    private final int maxTurns;
}