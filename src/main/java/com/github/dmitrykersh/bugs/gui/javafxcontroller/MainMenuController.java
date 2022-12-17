package com.github.dmitrykersh.bugs.gui.javafxcontroller;

import com.github.dmitrykersh.bugs.gui.SceneCollection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

public class MainMenuController {
    @FXML
    private void localGameButton_onPressed(ActionEvent event) {
        SceneCollection.switchToScene("local-game-menu", event);
    }

    @FXML
    private void onlineGameButton_onPressed(ActionEvent event) {
        SceneCollection.switchToScene("online-game-menu", event);
    }

    @FXML
    private void settingsButton_onPressed(ActionEvent event) {
        SceneCollection.switchToScene("settings-menu", event);
    }

    @FXML
    private void quitButton_onPressed(ActionEvent event) {
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.close();
    }
}
