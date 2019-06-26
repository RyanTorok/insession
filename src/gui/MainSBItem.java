package gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
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
        AutoColoredLabel content = new AutoColoredLabel(label, wrapper);
        content.setFont(Font.font("Sans Serif", Size.fontSize(24)));
        content.setTextFill(Colors.textFill(background));
        Styles.setBackgroundColor(this, background, 8);
        Events.highlightOnMouseOver(this);
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
