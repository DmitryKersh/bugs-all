package com.github.dmitrykersh.bugs.gui.controller;

import com.github.dmitrykersh.bugs.gui.SceneCollection;
import javafx.event.ActionEvent;

public class OnlineGameMenuController {
    public void backButton_onClick(ActionEvent event) {
        SceneCollection.switchToScene("main-menu", event);
    }
}