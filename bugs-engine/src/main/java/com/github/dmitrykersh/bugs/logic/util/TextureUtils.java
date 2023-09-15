package com.github.dmitrykersh.bugs.logic.util;

import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class TextureUtils {
    public static Blend makeBlend(final @NotNull Image base, final @NotNull Color color, double posX, double posY, double sizeX, double sizeY) {
        ColorInput colorStencil = new ColorInput();
        colorStencil.setHeight(base.getHeight());
        colorStencil.setWidth(base.getWidth());
        colorStencil.setPaint(color);
        colorStencil.setX(posX);
        colorStencil.setY(posY);
        colorStencil.setWidth(sizeX);
        colorStencil.setHeight(sizeY);

        Blend blend = new Blend();
        blend.setMode(BlendMode.MULTIPLY);
        blend.setBottomInput(colorStencil);

        return blend;
    }
}
