package gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class SubtleTextField extends TextField {

    private ColorThemeBase bindBackground;

    private static final Border BORDER_LIGHT = new Border(new BorderStroke(Color.TRANSPARENT, Color.TRANSPARENT, Color.DARKGRAY, Color.TRANSPARENT,
                      BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
                      CornerRadii.EMPTY, BorderWidths.DEFAULT, Insets.EMPTY));
    private static final Border BORDER_DARK = new Border(new BorderStroke(Color.TRANSPARENT, Color.TRANSPARENT, Color.LIGHTGRAY, Color.TRANSPARENT,
            BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
            CornerRadii.EMPTY, BorderWidths.DEFAULT, Insets.EMPTY));
    private static final Background BACKGROUND = new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY));

    public SubtleTextField() {
        this("");
    }


    public SubtleTextField(String text) {
        super(text);
        refresh();
        backgroundProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || !newValue.equals(BACKGROUND)) {
                refreshBackground();
            }
        });
        borderProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || !newValue.equals(whichBorder()))
                refreshBorder();
        });
    }

    private Border whichBorder() {
        if (getBindBackground() == null || !getBindBackground().isDarkMode()) return BORDER_LIGHT;
        else return BORDER_DARK;
    }



    private void refresh() {
        refreshBackground();
        refreshBorder();
    }

    private void refreshBorder() {
        setBorder(whichBorder());
    }

    private void refreshBackground() {
        setBackground(BACKGROUND);
    }

    public ColorThemeBase getBindBackground() {
        return bindBackground;
    }

    public void setBindBackground(ColorThemeBase bindBackground) {
        this.bindBackground = bindBackground;
    }
}
