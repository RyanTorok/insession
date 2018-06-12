package gui;

import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

import java.util.Random;

public class SnowParticle extends PrecipParticle {

    private AnchorPane holder;

    SnowParticle(AnchorPane holder) {
        super(holder, 1);
    }

    private SnowParticle(int size) {
        super(size, 1);
    }

    @Override
    protected Shape get() {
        Circle circle = new Circle();
        circle.setFill(Color.WHITE);
        circle.setRadius(4 * this.size);
        return circle;
    }
}
