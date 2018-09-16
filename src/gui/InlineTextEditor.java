package gui;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import main.Events;
import main.User;
import org.fxmisc.richtext.Selection;
import org.fxmisc.richtext.SelectionImpl;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.model.EditableStyledDocument;

public class InlineTextEditor extends VBox {

    private Node target;
    private final TextFlow source;
    private final Parent parent;
    private final int index;
    private StackPane controls;
    private StyledTextArea editor;

    public static void edit(Node target, TextFlow source) {
        Parent parent = target.getParent();
        if (!(parent instanceof Pane)) throw new IllegalCallerException("Parent of target node to edit inline does not extend Pane");
        int index = parent.getChildrenUnmodifiable().indexOf(target);
        if (index == -1)
            throw new IllegalArgumentException("Parent does not contain node of target to edit inline");
        ((Pane) parent).getChildren().set(index, new InlineTextEditor(target, source, parent, index));
        User.active().getKeyMap().lock();
    }


    public InlineTextEditor(Node target, TextFlow source, Parent parent, int index) {
        super();
        this.target = target;
        this.source = source;
        this.parent = parent;
        this.index = index;

        HBox textControls = new HBox();
        Text submit = new Text("Submit");
        Events.highlightOnMouseOver(submit);
        submit.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> submit());
        textControls.getChildren().add(submit);

        controls = new StackPane(textControls);

        editor = new StyledTextArea<>(null, (x, y) -> {}, null, (x, y) -> {});
        getChildren().addAll(controls, editor);
    }

    private void submit() {
        ((Pane) parent).getChildren().set(index, target);
        User.active().getKeyMap().lock();
    }

    public HTMLText encode() {
        return null; //TODO
    }

    public StackPane getControls() {
        return controls;
    }

    public void setControls(StackPane controls) {
        this.controls = controls;
    }

    public StyledTextArea getEditor() {
        return editor;
    }

    public void setEditor(StyledTextArea editor) {
        this.editor = editor;
    }
}
