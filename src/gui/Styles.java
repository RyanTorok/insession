package gui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import main.Colors;

import java.util.Objects;

public class Styles {

    public static String getProperty(Node n, String property) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(property);
        int[] propertyBounds = getPropertyBounds(n, property);
        if (propertyBounds == null)
            return "";
        return n.getStyle().substring(propertyBounds[1], propertyBounds[2] - 1).trim();
    }

    public static void setProperty(Node n, String property, String value) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(property);
        Objects.requireNonNull(value);
        int[] propertyBounds = getPropertyBounds(n, property);
        if (propertyBounds != null)
            n.setStyle(n.getStyle().substring(0, propertyBounds[1]) + value + n.getStyle().substring(propertyBounds[2] - 2));
        else //we may add extra semicolons because the SDK doesn't follow my rules here.
            n.setStyle(n.getStyle() + "; " + property + ": " + value + " ; ");

        //remove unnecessary semicolons
        n.setStyle(n.getStyle().replaceAll("; ;", ";"));
    }

    public static void removeProperty(Node n, String property) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(property);
        int[] propertyBounds = getPropertyBounds(n, property);
        if (propertyBounds == null)
            return;
        n.setStyle(n.getStyle().substring(0, propertyBounds[0]) + n.getStyle().substring(propertyBounds[2]));
        //remove unnecessary semicolons
        n.setStyle(n.getStyle().replaceAll("; ;", ";"));

    }

    private static int[] getPropertyBounds(Node n, String property) {
        int index = n.getStyle().indexOf(property);
        if (index == -1)
            return null;
        int[] returnVal = new int[3];
        returnVal[0] = index;
        returnVal[1] = index + property.length() + 2; //pass the colon and space
        int semi = n.getStyle().indexOf(';', returnVal[1]);
        returnVal[2] = semi == -1 ? n.getStyle().length() + 1 : semi + 1;
        return returnVal;
    }

    public static Color getBackgroundColor(Node n) {
        if (n instanceof Region && !(n instanceof ScrollPane)) {
            final Background background = ((Region) n).getBackground();
            if (background == null)
                return null;
            final Paint fill = background.getFills().get(0).getFill();
            if (fill instanceof Color)
                return (Color) fill;
            else return Color.TRANSPARENT;
        }
        String colorString = getProperty(n, "-fx-background-color");
        if (colorString.trim().equals("")) {
            return null;
        }
        if (colorString.equals("transparent")) {
            return Color.TRANSPARENT;
        }
        try {
            return Color.web(colorString);
        } catch (RuntimeException e) {
            return null;
        }
    }

    //TODO test alternate region implementation
    public static void setBackgroundColor(Node n, Color c) {
        setBackgroundColor(n, c, CornerRadii.EMPTY);
    }

    public static void setBackgroundColor(Node n, Color c, double radius) {
        setBackgroundColor(n, c, new CornerRadii(radius));
    }

    public static void setBackgroundColor(Node n, Color c, CornerRadii radii) {
        if (n instanceof Region && !(n instanceof ScrollPane))
            ((Region) n).setBackground(new Background(new BackgroundFill(c, radii, Insets.EMPTY)));
        else {
            setProperty(n, "-fx-background-color", Colors.colorToHex(c));
            setProperty(n, "-fx-background-radius", String.valueOf(radii.getTopLeftHorizontalRadius()));
        }
    }

    public static Border defaultBorder(Paint fill) {
        return new Border(new BorderStroke(fill, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
    }
}
