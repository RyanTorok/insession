package gui;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import main.Colors;

public class Styles {

    public static String getProperty(Node n, String property) {
        int[] propertyBounds = getPropertyBounds(n, property);
        if (propertyBounds == null)
            return "";
        return n.getStyle().substring(propertyBounds[1], propertyBounds[2] - 2); //subtract 2 for semicolon
    }

    public static void setProperty(Node n, String property, String value) {
        int[] propertyBounds = getPropertyBounds(n, property);
        if (propertyBounds != null)
            n.setStyle(n.getStyle().substring(0, propertyBounds[1]) + value + n.getStyle().substring(propertyBounds[2] - 2));
        else
            n.setStyle(n.getStyle() + property + ": " + value + " ; ");
    }

    public static void removeProperty(Node n, String property) {
        int[] propertyBounds = getPropertyBounds(n, property);
        if (propertyBounds == null)
            return;
        n.setStyle(n.getStyle().substring(0, propertyBounds[0]) + n.getStyle().substring(propertyBounds[2]));

    }

    private static int[] getPropertyBounds(Node n, String property) {
        int index = n.getStyle().indexOf(property);
        if (index == -1)
            return null;
        int[] returnVal = new int[3];
        returnVal[0] = index;
        returnVal[1] = index + property.length() + 2; //pass the colon and space
        int semi = n.getStyle().indexOf(';', returnVal[1]);
        returnVal[2] = semi == -1 ? n.getStyle().length() : semi + 1;
        return returnVal;
    }

    public static Color getBackgroundColor(Node n) {
        String colorString = getProperty(n, "-fx-background-color");
        if (colorString == null)
            return null;
        return Color.web(colorString);
    }

    public static void setBackgroundColor(Node n, Color c) {
        setProperty(n, "-fx-background-color", Colors.colorToHex(c));
    }
}
