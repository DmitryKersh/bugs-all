package com.github.dmitrykersh.bugs.gui.controller;

import com.github.dmitrykersh.bugs.api.board.layout.Layout;
import com.github.dmitrykersh.bugs.api.board.layout.PlayerConfig;
import com.github.dmitrykersh.bugs.gui.SceneCollection;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import lombok.val;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocalGameMenuController {
    private static final String LAYOUT_DIR = "src/main/resources/layout";
    @FXML
    public ComboBox<String> layoutComboBox;
    @FXML
    public ComboBox<String> playerConfigComboBox;
    @FXML
    public GridPane playersGridPane;

    @FXML
    public void initialize() {
        layoutComboBox.setItems(new ObservableListWrapper<>(getAvailableLayoutFiles()));
    }

    public void layoutComboBox_onChanged() {
        if (layoutComboBox.getValue() == null) {
            playerConfigComboBox.setItems(null);
            return;
        }

        Layout layout = new Layout(new HashMap<>());
        layout.LoadLayout(LAYOUT_DIR + "/" + layoutComboBox.getValue());
        val configs = layout.getPlayerConfigs();
        List<String> configDesc = new ArrayList<>();
        for (PlayerConfig config : configs)
            configDesc.add(config.toString());
        playerConfigComboBox.setItems(new ObservableListWrapper<>(configDesc));
    }

    public void playerConfigComboBox_onChanged() {
        // TODO somehow get player=amount and populate Gridpane
    }

    public void backButton_onClick(ActionEvent event) {
        SceneCollection.switchToScene("main-menu", event);
    }

    private List<String> getAvailableLayoutFiles() {
        File[] files = new File(LAYOUT_DIR).listFiles();
        if (files == null) return null;

        List<String> names = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            names.add(files[i].getName());
        }
        names.add(null);
        return names;
    }
}
