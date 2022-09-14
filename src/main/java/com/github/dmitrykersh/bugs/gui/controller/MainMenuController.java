package com.github.dmitrykersh.bugs.gui.controller;

import com.github.dmitrykersh.bugs.gui.SceneCollection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MainMenuController {
    @FXML
    private Button settingsButton;

    @FXML
    private void settingsButton_onPressed(ActionEvent event) {
        Stage mainStage = (Stage)((Node)event.getSource()).getScene().getWindow();
        mainStage.setScene(SceneCollection.getScene("settings-menu"));
    }
}
