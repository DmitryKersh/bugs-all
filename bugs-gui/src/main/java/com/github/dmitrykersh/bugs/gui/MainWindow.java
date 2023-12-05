package com.github.dmitrykersh.bugs.gui;

import com.github.dmitrykersh.bugs.engine.TextureCollection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

public class MainWindow extends Application {

    @Override
    public void start(final Stage stage) throws Exception {
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = this.getClass()
                    .getClassLoader()
                    .getResourceAsStream("config/config.yaml");
            ClientConfig config = yaml.load(inputStream);
            System.out.println("---------- CLIENT CONFIG ----------\n" + config);

            SceneCollection.loadScene("main-menu", "/fxml/main-menu.fxml");
            SceneCollection.loadScene("settings-menu", "/fxml/settings-menu.fxml");
            SceneCollection.loadScene("local-game-menu", "/fxml/local-game-menu.fxml");
            SceneCollection.loadScene("online-game-menu", "/fxml/online-game-menu.fxml");

            TextureCollection.loadImage("queen", "/textures/queen.png");
            TextureCollection.loadImage("bug", "/textures/bug.png");
            TextureCollection.loadImage("wall", "/textures/wall.png");
            TextureCollection.loadImage("empty", "/textures/empty.png");
            TextureCollection.loadImage("unavailable", "/textures/unavailable.png");

            stage.setMinWidth(800);
            stage.setMinHeight(720);

            stage.setHeight(config.getScreenHeight());
            stage.setWidth(config.getScreenWidth());

            stage.setMaximized(config.isMaximized());

            stage.setScene(SceneCollection.getScene("main-menu"));
            stage.setOnCloseRequest(windowEvent -> {
                Platform.exit();
                System.exit(0);
            });

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Application.launch();
    }
}
