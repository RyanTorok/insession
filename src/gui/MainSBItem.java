package gui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import main.Colors;
import main.Events;
import main.Layouts;
import main.Size;


public class MainSBItem extends HBox {

    private State state;

    public MainSBItem(ClassView wrapper, String label) {
        final Color background = wrapper.getBackgroundColor();
        Text content = new Text(label);
        content.setFont(Font.font("Sans Serif", Size.fontSize(24)));
        content.setFill(Colors.textFill(background));
        Styles.setBackgroundColor(this, background);
        Styles.setProperty(this, "-fx-background-radius", String.valueOf(8));
        Events.highlightOnMouseOver(this, wrapper.getBackgroundColor().brighter());
        state = State.UNSELECTED;
        this.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (state == State.UNSELECTED) {
                wrapper.selectSubSection(this);
            } else {
                wrapper.returnToHome();
            }
        });
        getChildren().addAll(new Layouts.Filler(), content, new Layouts.Filler());
        setPadding(Size.insets(10));

    }

    public void setState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    enum State {
        INVISIBLE, UNSELECTED, SELECTED
    }
}
