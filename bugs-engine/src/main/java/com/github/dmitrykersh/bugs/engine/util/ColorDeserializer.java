package com.github.dmitrykersh.bugs.engine.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.paint.Color;

import java.io.IOException;

public class ColorDeserializer extends JsonDeserializer<Color> {
    @Override
    public Color deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        double red = node.get("red").doubleValue();
        double green = node.get("green").doubleValue();
        double blue = node.get("blue").doubleValue();
        double opacity = node.get("opacity").doubleValue();
        return new Color(red, green, blue, opacity);
    }
}