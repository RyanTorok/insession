package gui;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import main.Colors;

public class AutoColoredTextFlow extends TextFlow {

    private Color onWhite;
    private Color onBlack;

    public AutoColoredTextFlow(Node bindBackground) {
        super();
        init(bindBackground);
    }


    public AutoColoredTextFlow(Node bindBackground, String text) {
        this(bindBackground, new Text(text));
    }

    public AutoColoredTextFlow(Node bindBackground, Text... text) {
        super(text);
        init(bindBackground);
    }

    public AutoColoredTextFlow(Node bindBackground, Label... text) {
        super(text);
        init(bindBackground);
    }

    private void init(Node bindBackground) {
        if (bindBackground instanceof Region) {
            ((Region) bindBackground).backgroundProperty().addListener((observable, oldValue, newValue) -> update(bindBackground));
        } else {
            bindBackground.styleProperty().addListener((observable, oldValue, newValue) -> update(bindBackground));
        }
        getChildrenUnmodifiable().addListener((ListChangeListener<Node>) c -> update(bindBackground));
        update(bindBackground);
    }

    private void update(Node bindBackground) {
        final Color defaultColor = Colors.textFill(Styles.getBackgroundColor(bindBackground));
        for (Node n : AutoColoredTextFlow.this.getChildrenUnmodifiable()) {
            if (n instanceof Text) {
                if (defaultColor == Color.WHITE)
                    ((Text) n).setFill(getOnWhite() == null ? defaultColor : getOnWhite());
                else
                    ((Text) n).setFill(getOnBlack() == null ? defaultColor : getOnBlack());
            } else if (n instanceof Label && !(n instanceof AutoColoredLabel)) {
                if (defaultColor == Color.WHITE)
                    ((Label) n).setTextFill(getOnWhite() == null ? defaultColor : getOnWhite());
                else
                    ((Label) n).setTextFill(getOnBlack() == null ? defaultColor : getOnBlack());
            }
        }
    }

    public Color getOnWhite() {
        return onWhite;
    }

    public void setOnWhite(Color onWhite) {
        this.onWhite = onWhite;
    }

    public Color getOnBlack() {
        return onBlack;
    }

    public void setOnBlack(Color onBlack) {
        this.onBlack = onBlack;
    }

    public void setFont(Font font) {
        for (Node n : getChildrenUnmodifiable()) {
            if (n instanceof Text)
                ((Text) n).setFont(font);
            else if (n instanceof Label)
                ((Label) n).setFont(font);
        }
    }
}
