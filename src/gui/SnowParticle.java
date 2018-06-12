package gui;

import javafx.animation.TranslateTransition;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.Random;

public class SnowParticle extends Circle {

    private AnchorPane holder;

    SnowParticle(AnchorPane holder) {
        this(new Random().nextInt(6) + 1);
        this.holder = holder;
    }

    private SnowParticle(int size) {
        if (size < 3)
            size = 1;
        else if (size < 5)
            size = 2;
        else size = 3;
        setFill(Color.WHITE);
        setRadius(4 * size);

        //random start point
        double rand = Math.random() * (1920 + 1080);
        if (rand < 1080)
            AnchorPane.setTopAnchor(this, rand);
        else AnchorPane.setLeftAnchor(this, rand - 1080);
        TranslateTransition move = new TranslateTransition();
        move.setDuration(Duration.seconds((4 - size) * 3 * (Math.random() / 4 + .875)));
        move.setNode(this);
        move.setByX(2000 + (Math.random() * 500) - 250);
        move.setByY(2000);
//        move.setByZ(Math.random() < .5 ? 200 : -200);
        move.play();
        move.setOnFinished(event -> holder.getChildren().remove(this));
    }
}
