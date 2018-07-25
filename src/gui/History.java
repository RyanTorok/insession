package gui;

import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class History extends TaskView {
    public History() {
        super("History");
    }

    public Pane getFullDisplay() {
        return null;
    }

    @Override
    ImageView getPlaceholderImage() {
        return null;
    }
}
