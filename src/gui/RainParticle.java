package gui;

import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

import java.util.Random;

public class RainParticle extends PrecipParticle {

    private AnchorPane holder;

    RainParticle(AnchorPane holder) {
        super(holder, 5);
    }

    private RainParticle(int size) {
        super(size, 4);
    }

    protected Shape get() {
        Line l = new Line();
        l.setStroke(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.4));
        l.setStrokeWidth(size);
        l.setStartX(0);
        l.setStartY(0);
        l.setEndX(7.5 * size);
        l.setEndY(15 * size);
        return l;
    }
}
