package classes;

import gui.RichText;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class InlineTextEditor extends VBox {

    private StackPane controls;
    private TextArea textArea;

    public InlineTextEditor() {
        super();

        HBox enablecControls = new HBox();
        HBox textControls = new HBox();

        controls = new StackPane(textControls, enablecControls);
        getChildren().addAll(controls, textArea);
    }

    public InlineTextEditor(RichText existingText) {
        this();
    }

    public RichText encode() {
        return null; //TODO
    }
}
