package com.github.dmitrykersh.bugs.gui;

import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class TextureCollection {
    private static final Map<String, Image> textures = new HashMap<>();
    private TextureCollection(){}

    public static void loadImage(final @NotNull String name, final @NotNull String imagePath) {
        URL url = TextureCollection.class.getResource(imagePath);
        if (url != null) {
            textures.put(name, new Image(url.toString()));
        } else {
            //TODO: handle it somehow
        }
    }

    public static Image getImageByName(final @NotNull String name) {
        return textures.get(name);
    }

}
