package com.github.dmitrykersh.bugs.gui;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UiUtils {
    public static Label makeLabel(String text, int fontsize, Color textColor) {
        javafx.scene.control.Label l = new javafx.scene.control.Label(text);
        l.setFont(Font.font("Consolas", fontsize));
        l.setTextFill(textColor);
        return l;
    }

    public static Label makeLabel(String text, int fontsize) {
        javafx.scene.control.Label l = new javafx.scene.control.Label(text);
        l.setFont(Font.font("Consolas", fontsize));
        l.setTextFill(Color.WHITE);
        return l;
    }
}
