package com.github.dmitrykersh.bugs.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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
}
