package classes;

import gui.RichText;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;

public class InlineTextEditor extends VBox {

    private StackPane controls;
    private TextArea textArea;

    public static void edit(Node target, TextFlow source) {
        Parent parent = target.getParent();
        if (!(parent instanceof Pane)) throw new IllegalCallerException("Parent of target node to edit inline does not extend Pane");
        int index = parent.getChildrenUnmodifiable().indexOf(target);
        if (index == -1)
            throw new IllegalArgumentException("Parent does not contain node of target to edit inline");
        ((Pane) parent).getChildren().set(index, new InlineTextEditor());
    }

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
