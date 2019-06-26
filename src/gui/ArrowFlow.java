package gui;

import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import main.Layouts;

import java.util.ArrayList;

public class ArrowFlow extends BorderPane {

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    private static final int UP = 0;
    private static final int DOWN = 1;
    private static final int LEFT = 2;
    private static final int RIGHT = 3;

    private boolean previousEnabled;
    private boolean nextEnabled;

    public ArrowFlow(ArrayList<? extends Pane> elements, int direction) {
        if (direction != HORIZONTAL && direction != VERTICAL)
            throw new IllegalArgumentException("unexpected direction parameter");
        if (direction == HORIZONTAL) {
            
        } else {

        }
    }

    private static class ArrowBox extends VBox {
        ArrowBox(int direction) {
            Styles.setBackgroundColor(this, Color.LIGHTGRAY);
            final Text arrow = new Text(Character.toString((char) ('▲' + direction)));
            getChildren().add(arrow);
            setAlignment(Pos.CENTER);
            getChildren().addAll(new Layouts.Filler(), arrow, new Layouts.Filler());

        }
    }



}
