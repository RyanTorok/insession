package gui;

import javafx.animation.TranslateTransition;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.util.Random;

public abstract class PrecipParticle {
    final boolean day;
    Shape shape;
    int size;
    private AnchorPane holder;

    PrecipParticle(AnchorPane holder, double speed, boolean day) {
        this(new Random().nextInt(6) + 1, speed, day);
        this.holder = holder;
    }

    public PrecipParticle(int size, double speed, boolean day) {
        if (size < 3)
            this.size = 1;
        else if (size < 5)
            this.size = 2;
        else this.size = 3;
        this.day = day;
        shape = get();

        //random start point
        double rand = Math.random() * (1920 + 1080);
        if (rand < 1080)
            AnchorPane.setTopAnchor(shape, rand);
        else AnchorPane.setLeftAnchor(shape, rand - 1080);
        TranslateTransition move = new TranslateTransition();
        move.setDuration(Duration.seconds((1.0 / speed) * ((4 - this.size) * 3 * (Math.random() / 4 + .875))));
        move.setNode(shape);
        move.setByX(1000 + (Math.random() * 500) - 250);
        move.setByY(2000);
        move.play();
        move.setOnFinished(event -> holder.getChildren().remove(shape));

    }

    protected abstract Shape get();
}