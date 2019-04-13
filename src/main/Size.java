package main;

import gui.Styles;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.Window;

/**
 * Utility class for scalable size calculations. Actual scalability data is in UtilAndConstants.java.
 * Methods here only call this class, where the actual calculations are done.
 */

public class Size {

    public static final double DEFAULT_WIDTH = 1920;
    public static final double DEFAULT_HEIGHT = 1080;
    
    private double screenWidth = DEFAULT_WIDTH, screenHeight = DEFAULT_HEIGHT;

    public static double fontSize(double i) {
        return Root.sizeInstance().fontSize_obj(i);
    }

    public static double width(double width) {
        return Root.sizeInstance().width_obj(width);
    }

    public static double height(double height) {
        return Root.sizeInstance().height_obj(height);
    }

    public static Insets insets(double i) {
        return insets(i, i);
    }

    public static Insets insets(double top, double right, double bottom, double left) {
        return new Insets(height(top), width(right), height(bottom), width(left));
    }

    public static Insets insets(double horizontal, double vertical) {
        double w = width(horizontal), h = height(vertical);
        return new Insets(h, w, h, w);
    }

    public static double lessWidthHeight(double i) {
        return Math.min(width(i), height(i));
    }

    public static double scaledWidth(double actualWidth) {
        return Root.sizeInstance().scaledWidth_obj(actualWidth);
    }

    public static double scaledHeight(double actualHeight) {
        return Root.sizeInstance().scaledHeight_obj(actualHeight);
    }

    public static double fontDimension(double widthHeight) {
        return Root.sizeInstance().fontDimension_obj(widthHeight);
    }
    
    public void updateScreenSize() {
        Window w = Root.getPortal().getCurrentWindow();
        updateScreenSize(w.getWidth(), w.getHeight());
    }

    public synchronized void updateScreenSize(double width, double height) {
        //iterate through the scene graph and set size constraints
        if (Root.getPortal() != null) {
            Pane mainArea = Root.getPortal().getMainArea();
            updateSizeAllChildren(mainArea, width, height);
        }
        screenWidth = width;
        screenHeight = height;
    }

    private void updateSizeAllChildren(Region r, double width, double height) {
        for (Node n : r.getChildrenUnmodifiable()) {
            if (n instanceof Region) {
                resize((Region) n, width, height);
            }
            if (n instanceof Text) {
                String oldSizeStr = Styles.getProperty(n, "-fx-font-size");
                double oldSize = oldSizeStr.length() > 0 ? Double.parseDouble(oldSizeStr.trim()) : ((Text) n).getFont().getSize();
                double scaledSize = scaledFontSize_obj(oldSize);
                if (oldSize != -1) {
                    double newSize = Math.min(scaledSize * width / DEFAULT_WIDTH, scaledSize * height / DEFAULT_HEIGHT);
                    Styles.setProperty(n, "-fx-font-size", String.valueOf(newSize));
                }
            }
            if (n instanceof TextField) {
                double oldSize = ((TextField) n).getFont().getSize();
                double scaledSize = scaledFontSize_obj(oldSize);
                if (oldSize != -1) {
                    double newSize = Math.min(scaledSize * width / DEFAULT_WIDTH, scaledSize * height / DEFAULT_HEIGHT);
                    Styles.setProperty(n, "-fx-font-size", String.valueOf(newSize));
                }
            }
            if (n instanceof Circle) {
                //which direction is limiting the size?
                double desiredXRadius = ((Circle) n).getRadius() * DEFAULT_WIDTH / screenWidth;
                double desiredYRadius = ((Circle) n).getRadius() * DEFAULT_HEIGHT / screenHeight;
                double originalRadius = Math.max(desiredXRadius, desiredYRadius);
            ((Circle) n).setRadius(originalRadius * Math.min(width / DEFAULT_WIDTH, height / DEFAULT_HEIGHT));
            }
            if (AnchorPane.getTopAnchor(n) != null) {
                AnchorPane.setTopAnchor(n, convertHeight(AnchorPane.getTopAnchor(n), height));
            }
            if (AnchorPane.getBottomAnchor(n) != null) {
                AnchorPane.setBottomAnchor(n, convertHeight(AnchorPane.getBottomAnchor(n), height));
            }
            if (AnchorPane.getLeftAnchor(n) != null) {
                AnchorPane.setLeftAnchor(n, convertWidth(AnchorPane.getLeftAnchor(n), width));
            }
            if (AnchorPane.getRightAnchor(n) != null) {
                AnchorPane.setRightAnchor(n, convertWidth(AnchorPane.getRightAnchor(n), width));
            }
        }
    }

    private void resize(Region r, double width, double height) {
        if (r.getPrefWidth() != Region.USE_COMPUTED_SIZE) {
            r.setPrefWidth(convertWidth(r.getPrefWidth(), width));
        }
        if (r.getPrefHeight() != Region.USE_COMPUTED_SIZE) {
            r.setPrefHeight(convertHeight(r.getPrefHeight(), height));
        }
        if (r.getMinWidth() != Region.USE_COMPUTED_SIZE && r.getMinWidth() != Region.USE_PREF_SIZE) {
            r.setMinWidth(convertWidth(r.getMinWidth(), width));
        }
        if (r.getMinHeight() != Region.USE_COMPUTED_SIZE && r.getMinHeight() != Region.USE_PREF_SIZE) {
            r.setMinHeight(convertHeight(r.getMinHeight(), height));
        }

        if (r.getMaxWidth() != Region.USE_COMPUTED_SIZE) {
            r.setMaxWidth(convertWidth(r.getMaxWidth(), width));
        }
        if (r.getMaxHeight() != Region.USE_COMPUTED_SIZE) {
            r.setMaxHeight(convertHeight(r.getMaxHeight(), height));
        }
        if (r.getTranslateX() != 0) {
            r.setTranslateX(convertWidth(r.getTranslateX(), width));
        }
        if (r.getTranslateY() != 0) {
            r.setTranslateY(convertHeight(r.getTranslateY(), height));
        }
        if (r.getLayoutX() != 0) {
            r.setLayoutX(convertWidth(r.getLayoutX(), width));
        }
        if (r.getLayoutY() != 0) {
            r.setLayoutY(convertHeight(r.getLayoutY(), height));
        }
        if (!(r.getPadding().equals(Insets.EMPTY))) {
            Insets old = r.getPadding();
            //we hack a 1 in one position to allow upsizing later. Otherwise the EMPTY check would be true even though values were set.
            r.setPadding(new Insets(Math.max(1, convertHeight(old.getTop(), height)), convertWidth(old.getRight(), width), convertHeight(old.getBottom(), height), convertWidth(old.getLeft(), width)));
        }
        if (r instanceof HBox) {
            double oldSpacing = ((HBox) r).getSpacing();
            if (oldSpacing != 0)
                ((HBox) r).setSpacing(Math.max(1, convertWidth(oldSpacing, width)));
        }
        if (r instanceof VBox) {
            double oldSpacing = ((VBox) r).getSpacing();
            if (oldSpacing != 0)
                ((VBox) r).setSpacing(Math.max(1, convertHeight(oldSpacing, height)));
        }
        if (r instanceof GridPane) {
            if (((GridPane) r).getHgap() != 0)
                ((GridPane) r).setHgap(convertWidth(((GridPane) r).getHgap(), width));
            if (((GridPane) r).getVgap() != 0)
                ((GridPane) r).setVgap(convertHeight(((GridPane) r).getVgap(), height));
        }
        updateSizeAllChildren(r, width, height);
    }

    private double convertWidth(double actualValue, double newScale) {
        return actualValue * newScale / screenWidth;
    }

    private double convertHeight(double actualValue, double newScale) {
        return actualValue * newScale / screenHeight;
    }

    public double getScreenWidth() {
        return screenWidth;
    }

    public double getScreenHeight() {
        return screenHeight;
    }
    
    double height_obj(double height) {
        return height * screenHeight / DEFAULT_HEIGHT;
    }

    double width_obj(double width) {
        return width * screenWidth / DEFAULT_WIDTH;
    }

    double fontSize_obj(double fontSize) {
        return Math.min(fontSize * screenWidth / DEFAULT_WIDTH, fontSize * screenHeight / DEFAULT_HEIGHT);
    }

    double scaledFontSize_obj(double actualFontSize) {
        if (screenWidth / DEFAULT_WIDTH < screenHeight / DEFAULT_HEIGHT) {
            return actualFontSize * DEFAULT_WIDTH / screenWidth;
        }
        return actualFontSize * DEFAULT_HEIGHT / screenHeight;
    }

    double scaledWidth_obj(double actualWidth) {
        return actualWidth * DEFAULT_WIDTH / screenWidth;
    }

    double scaledHeight_obj(double actualHeight) {
        return actualHeight * DEFAULT_HEIGHT / screenHeight;
    }

    double fontDimension_obj(double widthHeight) {
        return widthHeight * Math.min(screenHeight / DEFAULT_HEIGHT, screenWidth / DEFAULT_WIDTH);
    }
}
