package gui;

import javafx.scene.CacheHint;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

import java.util.Random;

public class RainParticle extends PrecipParticle {

    RainParticle(AnchorPane holder, boolean day) {
        super(holder, 5, day);
    }

    protected Shape get() {
        Line l = new Line();
        Color stroke = (Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.4));
        if (!this.day)
            stroke = stroke.darker();
        l.setStroke(stroke);
        l.setStrokeWidth(size);
        l.setStartX(0);
        l.setStartY(0);
        l.setEndX(7.5 * size);
        l.setEndY(15 * size);
       return l;
    }
}
