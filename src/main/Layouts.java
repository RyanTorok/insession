package main;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class Layouts {
    public static class Filler extends Region {
        public Filler() {
            HBox.setHgrow(this, Priority.ALWAYS);
        }
    }
}
