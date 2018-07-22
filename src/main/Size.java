package main;

import javafx.geometry.Insets;

public class Size {
    public static double fontSize(int i) {
        return Root.getUtilAndConstants().fontSize(i);
    }

    public static double width(double width) {
        return Root.getUtilAndConstants().width(width);
    }

    public static double height(double height) {
        return Root.getUtilAndConstants().height(height);
    }

    public static Insets insets(double i) {
        return insets(i, i);
    }

    public static Insets insets(double top, double right, double bottom, double left) {
        return new Insets(height(top), width(right), height(bottom), width(left));
    }

    public static Insets insets(double vertical, double horizontal) {
        double w = width(horizontal), h = height(vertical);
        return new Insets(h, w, h, w);
    }

    public static double lessWidthHeight(double i) {
        return Math.min(width(i), height(i));
    }
}
