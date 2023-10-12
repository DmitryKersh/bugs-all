package com.github.dmitrykersh.bugs.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SceneCollection {
    private static final Map<String, Scene> scenes = new HashMap<>();
    private SceneCollection(){}

    public static void loadScene(final @NotNull String name, final @NotNull String url) {
        try {
            scenes.put(name, FXMLLoader.load(SceneCollection.class.getResource(url)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Scene getScene(final @NotNull String name) {
        return scenes.get(name);
    }

    public static void switchToScene(String name, ActionEvent e) {
        Stage mainStage = (Stage)((Node)e.getSource()).getScene().getWindow();
        val scene = SceneCollection.getScene(name);

        double w = mainStage.getWidth();
        double h = mainStage.getHeight();
        mainStage.setScene(scene);

        if (h > scene.getHeight()){
            mainStage.setHeight(h);
        }

        if (w > scene.getWidth()) {
            mainStage.setWidth(w);
        }
    }
}
