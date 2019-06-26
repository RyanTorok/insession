package main;

import gui.ColorThemeBase;
import gui.Styles;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.stream.Collectors;

public class Colors {

    private static final Color DISABLED = null;

    public static Color highlightColor(Color c) {
        //special case to avoid dividing by zero
        if (c.equals(Color.BLACK))
            return Color.web("#505050");
        Color c1 = textFill(c).equals(Color.WHITE) ? c.brighter() : c.darker();
        final double difference = Math.abs(difference(c, c1));
        if (difference < 0.15) {
            double scale = 0.15 / difference;
            return new Color(
                    c.getRed() + scale * (c1.getRed() - c.getRed()),
                    c.getGreen() + scale * (c1.getGreen() - c.getGreen()),
                    c.getBlue() + scale * (c1.getBlue() - c.getBlue()),
                    c.getOpacity()
            );
        }
        return c1;
    }

    private static double difference(Color c1, Color c2) {
        return (c2.getRed() - c1.getRed() + c2.getGreen() - c1.getGreen() + c2.getBlue() - c1.getBlue()) / 3;
    }


    public static Color textFill(Color background) {
        return textFill(background, 1.5);
    }

    public static Color textFill(Color background, double threshold) {
        if (background == null)
            return Color.BLACK;
        return background.getRed() + background.getGreen() + background.getBlue() > threshold ? Color.BLACK : Color.WHITE;
    }

    //returns an fxml string of the argument color and opacity. Note this format is incompatible with the highlightOnMouseOver() method.
    public static String rgba(Color color, double opacity) {
        return "rgba(" + color.getRed() * 255 + ", " + color.getGreen() * 255 + ", " + color.getBlue() * 255 + ", " + opacity + ")";
    }

    public static String colorToHex(Color color) {
        return String.format("#%02X%02X%02X",
                Math.min(255, (int) (color.getRed() * 256)),
                Math.min(255, (int) (color.getGreen() * 256)),
                Math.min(255, (int) (color.getBlue() * 256)));
    }

    private static HashMap<Region, Pair<Color, Color>> customInverts = new HashMap<>();
    private static HashMap<Region, Pair<Border, Border>> customBorders = new HashMap<>();

    //TODO resticting to regions is just to be "safe". There may be no side effects if we support arbitrary Node objects.
    public static void setCustomInvertColor(Region r, Color darkModeColor) {
        setCustomInvertColor(r, Styles.getBackgroundColor(r), darkModeColor);
    }

    public static void setCustomInvertColor(Region r, Color darkModeColor, Border darkModeBorder) {
        final Border border = r.getBorder();
        setCustomInvertColor(r, Styles.getBackgroundColor(r), border == null ? Border.EMPTY : border, darkModeColor, darkModeBorder);
    }

    public static void setCustomInvertColor(Region r, Color lightModeColor, Color darkModeColor) {
        if (lightModeColor == null)
            lightModeColor = Color.TRANSPARENT;
        customInverts.put(r, new Pair<>(lightModeColor, darkModeColor));
    }

    public static void setCustomInvertColor(Region r, Color lightModeColor, Border lightModeBorder, Color darkModeColor, Border darkModeBorder) {
        if (lightModeColor == null)
            lightModeColor = Color.TRANSPARENT;
        customInverts.put(r, new Pair<>(lightModeColor, darkModeColor));
        customBorders.put(r, new Pair<>(lightModeBorder, darkModeBorder));
    }

    public static void disableInvertColor(Region r) {
        setCustomInvertColor(r, Styles.getBackgroundColor(r), DISABLED);
    }

    public static void autoThemeSet(Node n, ColorThemeBase base) {
        if (base.isDarkMode())
            invertGrayscale(n);
    }

    public static void invertGrayscale(Node n) {
        if (n == null)
            return;
        Color custom = null;
        Boolean direction = null;
        if (n instanceof Region) {
            Pair<Color, Color> colorPair = customInverts.get(n);
            if (colorPair != null) {
                if (colorPair.getValue() == DISABLED)
                    return;
                if (colorPair.getValue().equals(Styles.getBackgroundColor(n))) {
                    custom = colorPair.getKey();
                    direction = false;
                } else {
                    custom = colorPair.getValue();
                    direction = true;
                }
            }
        }
        /*if (n instanceof Text) {
            final Text text = (Text) n;
            if (text.getFill() != null && text.getFill() instanceof Color) {
//                    if (!text.getFill().equals(Color.TRANSPARENT))
                //text.setFill(invert(((Color) text.getFill())));
            }
        }
        if (n instanceof Label) {
            final Label text = (Label) n;
            if (text.getTextFill() != null && text.getTextFill() instanceof Color)
                text.setTextFill(invert(((Color) text.getTextFill())));

        }*/
        if (n instanceof Region) {
            if (n instanceof TextField) {
                String property = Styles.getProperty(n, "-fx-text-fill");
                Color old = property.length() == 0 ? Color.BLACK : Color.web(property);
                Styles.setProperty(n, "-fx-text-fill", colorToHex(invertColor(old)));

                property = Styles.getProperty(n, "-fx-background-color");
                old = property.length() == 0 ? Color.WHITE : Color.web(property);
                Styles.setProperty(n, "-fx-background-color", colorToHex(custom == null ? invertColor(old) : custom));
            } else {
                Color backgroundColor = Styles.getBackgroundColor(n);
                if (backgroundColor != null) {
                    CornerRadii radii = CornerRadii.EMPTY;
                    if (((Region) n).getBackground() != null)
                        radii = ((Region) n).getBackground().getFills().get(0).getRadii();
                    Styles.setBackgroundColor(n, custom == null ? invertColor(backgroundColor) : custom, radii);
                }
            }
            //get custom border if necessary
            Border customBorder = null;
            Pair<Border, Border> borderPair = customBorders.get(n);
            if (borderPair != null && direction != null) {
                customBorder = direction ? borderPair.getValue() : borderPair.getKey();
            }
            if (customBorder != null) {
                ((Region) n).setBorder(customBorder);
            } else {
                Border old = ((Region) n).getBorder();
                if (old != null) {
                    Border newBorder = new Border(old.getStrokes().stream().map(Colors::invertBorderStrokes).collect(Collectors.toList()).toArray(new BorderStroke[]{}));
                    ((Region) n).setBorder(newBorder);
                }
            }
            for (Node child : ((Region) n).getChildrenUnmodifiable()) {
                invertGrayscale(child);
            }

        }
        if (n instanceof Shape && !(n instanceof Text)) {
            if (((Shape) n).getStroke() != null)
                ((Shape) n).setStroke(Colors.invert(((Shape) n).getStroke()));
/*            if (((Shape) n).getFill() != null)
                ((Shape) n).setFill(Colors.invert(((Shape) n).getFill()));*/
        }
    }


    private static Paint invertBorderStroke(Paint stroke) {
        return invert(stroke);
    }

    public static BorderStroke invertBorderStrokes(BorderStroke stroke) {
        return new BorderStroke(
                invertBorderStroke(stroke.getTopStroke()),
                invertBorderStroke(stroke.getRightStroke()),
                invertBorderStroke(stroke.getBottomStroke()),
                invertBorderStroke(stroke.getLeftStroke()),
        stroke.getTopStyle(), stroke.getRightStyle(), stroke.getBottomStyle(), stroke.getLeftStyle(), stroke.getRadii(), stroke.getWidths(), stroke.getInsets());
    }

    public static Color invertColor(Color c) {
        return (Color) invert(c);
    }

    public static Paint invert(Paint p) {
        if (p instanceof Color) {
            Color c = ((Color) p);
            return new Color(1 - c.getRed(), 1 - c.getGreen(), 1 - c.getBlue(), c.getOpacity());
        }
        return p;
    }

    private static  boolean isGrayscale(Color c) {
        return c.getRed() == c.getGreen() && c.getRed() == c.getBlue();
    }



}
