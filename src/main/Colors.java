package main;

import javafx.scene.paint.Color;

public class Colors {

    public static Color highlightColor(Color c) {
        if (c.equals(Color.BLACK))
            return Color.web("#505050");
        else return textFill(c).equals(Color.WHITE) ? c.brighter() : c.darker();
    }

    public static Color textFill(Color background) {
        return textFill(background, 1.5);
    }

    public static Color textFill(Color background, double threshold) {
        return background.getRed() + background.getGreen() + background.getBlue() > threshold ? Color.BLACK : Color.WHITE;
    }

    //returns an fxml string of the argument color and opacity. Note this format is incompatible with the highlightOnMouseOver() method.
    public static String rgba(Color color, double opacity) {
        return "rgba(" + color.getRed() * 255 + ", " + color.getGreen() * 255 + ", " + color.getBlue() * 255 + ", " + opacity + ")";
    }

    public static String colorToHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}
