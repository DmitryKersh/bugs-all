package com.github.dmitrykersh.bugs.gui.javafxcontroller;

import com.github.dmitrykersh.bugs.gui.SceneCollection;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.List;

public class OnlineGameMenuController {
    @FXML
    public TextField serverField;
    @FXML
    public TextField usernameField;
    @FXML
    public TextField nicknameField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public Button connectButton;
    @FXML
    public TabPane tabPane;
    @FXML
    public TextField gameIdField;
    @FXML
    public GridPane playersGridPane;
    @FXML
    public ComboBox<String> layoutComboBox;
    @FXML
    public ComboBox<String> playerConfigComboBox;
    @FXML
    public Button searchGameButton;
    @FXML
    public GridPane paramGridPane;
    @FXML
    public Label errorLabel;

    @FXML
    public void initialize() {
        //layoutComboBox.setItems(new ObservableListWrapper<>(getAvailableLayouts()));
    }

    private List<String> getAvailableLayouts() {
        return null;
    }

    public void backButton_onClick(ActionEvent event) {
        SceneCollection.switchToScene("main-menu", event);
    }

    public void layoutComboBox_onChanged(ActionEvent actionEvent) {
    }

    public void playerConfigComboBox_onChanged(ActionEvent actionEvent) {
    }

    public void startGame(ActionEvent actionEvent) {
    }
}
