package gui;

import javafx.scene.CacheHint;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

import java.util.Random;

public class SnowParticle extends PrecipParticle {

    private AnchorPane holder;

    SnowParticle(AnchorPane holder, boolean day) {
        super(holder, 1, day);
    }

    @Override
    protected Shape get() {
        Circle circle = new Circle();
        Color fill = Color.WHITE;
        if (!this.day)
            fill = fill.darker();
        circle.setFill(fill);
        circle.setRadius(4 * this.size);
        return circle;
    }
}
