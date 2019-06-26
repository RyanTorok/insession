package gui;

import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import main.Colors;

public class AutoColoredLabel extends Label {
    private Color onWhite;
    private Color onBlack;

    private ObjectProperty<Paint> background;

    public AutoColoredLabel(Node bindBackground) {
        this("", bindBackground);
    }

    public AutoColoredLabel(String text, Node bindBackground) {
        super(text);
        if (bindBackground instanceof Region) {
            ((Region) bindBackground).backgroundProperty().addListener((observable, oldValue, newValue) -> update(bindBackground));
        } else {
            bindBackground.styleProperty().addListener((observable, oldValue, newValue) -> update(bindBackground));
        }
        update(bindBackground);
    }

    private void update(Node bindBackground) {
        final Color defaultColor = Colors.textFill(Styles.getBackgroundColor(bindBackground));
        if (defaultColor == Color.WHITE)
            AutoColoredLabel.this.setTextFill(getOnWhite() == null ? defaultColor : getOnWhite());
        else
            AutoColoredLabel.this.setTextFill(getOnBlack() == null ? defaultColor : getOnBlack());
    }

    public AutoColoredLabel(String text, Node bindBackground, Color onWhite, Color onBlack) {
        this(text, bindBackground);
        this.setOnWhite(onWhite);
        this.setOnBlack(onBlack);
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
}
