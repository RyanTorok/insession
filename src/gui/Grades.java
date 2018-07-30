package gui;

import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class Grades extends TaskView {
    public Grades() {
        super("My grades");
    }

    public Pane getFullDisplay() {
        return null;
    }

    @Override
    ImageView getPlaceholderImage() {
        return null;
    }

    @Override
    protected Pane initDisplay() {
        return null;
    }
}
