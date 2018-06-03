package gui;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Shape;

public class ShapeImage {
    private Image i;
    private Shape s;

    public ShapeImage (Shape s, Image i) {
       this.setI(i);
       this.setS(s);
    }

    public Shape apply() {
        double size = Math.min(i.getWidth(), i.getHeight());
        s.setFill(new ImagePattern(i, 0, 0, 1, 1, true));
        return s;
    }

    public Image getI() {
        return i;
    }

    public void setI(Image i) {
        this.i = i;
    }

    public Shape getS() {
        return s;
    }

    public void setS(Shape s) {
        this.s = s;
    }
}
