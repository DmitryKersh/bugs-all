package com.github.dmitrykersh.bugs.gui.javafxcontroller;

import com.github.dmitrykersh.bugs.gui.SceneCollection;
import javafx.event.ActionEvent;

public class SettingsMenuController {
    public void backButton_onClick(ActionEvent event) {
        SceneCollection.switchToScene("main-menu", event);
    }
}