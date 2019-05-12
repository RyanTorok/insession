package main;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.HashSet;

public class Events {

    private static HashSet<Animation> animations;

    public static void initAnimations() {
        animations = new HashSet<>();
        Runnable resizeHandler = () -> {
            while (true) {
                while (Root.getPortal() == null || Root.getPortal().getPrimaryStage() == null || Root.getPortal().getPrimaryStage().isResizable())
                    Thread.yield();
                if (animations.isEmpty() && Root.getPortal() != null && Root.getPortal().getPrimaryStage() != null) {
                    //just a hack to get the resize setting to run on the JFX Application Thread
                    Task<Boolean> dummy = new Task<>() {
                        @Override
                        protected Boolean call() {
                            return true;
                        }
                    };
                    dummy.setOnSucceeded(event -> {
                        //TODO do something here
                    });
                    new Thread(dummy).start();
                }
            }
        };
        new Thread(resizeHandler).start();
    }

    public static void fireMouse(Node n, EventType<MouseEvent> type) {
        n.fireEvent(new MouseEvent(type, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
    }

    public static void highlightOnMouseOver(Node n, Color... target) {
        n.setCursor(Cursor.CLOSED_HAND);
        if (n instanceof Text && ((Text) n).getFill() instanceof Color) {
            Color orig = (Color) ((Text) n).getFill();
            n.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> ((Text) n).setFill(target.length == 0 ? Colors.highlightColor(orig) : target[0]));
            n.addEventHandler(MouseEvent.MOUSE_EXITED, event -> ((Text) n).setFill(orig));
            return;
        }
        String oldStyle_ = n.getStyle();
        if (!oldStyle_.contains("-fx-background-color")) {
            if (n.getStyle() == null || n.getStyle().length() == 0)
                n.setStyle("-fx-background-color: #000000");
            else n.setStyle(n.getStyle() + "; -fx-background-color: #000000");
            oldStyle_ = n.getStyle();
        }
        final String oldStyle = oldStyle_;
        int colorIndex = n.getStyle().indexOf("-fx-background-color: #") + 22;
        String oldColorStr = n.getStyle().substring(colorIndex, colorIndex + 7);
        Color oldColor = Color.web(oldColorStr);
        String newColorStr = Colors.colorToHex(target.length == 0 ? Colors.highlightColor(oldColor) : target[0]);
        String newStyle = oldStyle.replaceAll("-fx-background-color: #......", "-fx-background-color: " + newColorStr);;
        n.addEventHandler(MouseEvent.MOUSE_EXITED, event -> n.setStyle(oldStyle));
        n.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> n.setStyle(newStyle));
    }

    public static void underlineOnMouseOver(Text text) {
        text.setCursor(Cursor.CLOSED_HAND);
        text.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> text.setUnderline(true));
        text.addEventHandler(MouseEvent.MOUSE_EXITED, event -> text.setUnderline(false));
    }

    public static void animation(Animation a) {
        animation(a, true);
    }

    static HashMap<Animation, Integer> debug = new HashMap<>();

    public static void animation(Animation a, boolean delayResize) {
        if (a.getCycleCount() != Animation.INDEFINITE && delayResize) {
            animations.add(a);
            final EventHandler<ActionEvent> onFinished = a.getOnFinished();
            a.setOnFinished(event -> {
                if (onFinished != null)
                    onFinished.handle(event);
                animations.remove(a);
            });
        }
        a.play();
    }

    static HashSet<Animation> getActiveAnimations() {
        return animations;
    }

    public static void translate(Node n, TranslateMode mode, Double x, Double y, Duration duration) {
        translate(n, mode, x, y, duration, null);
    }

    public static void translate(Node n, TranslateMode mode, Double x, Double y, Duration duration, EventHandler<ActionEvent> onFinished) {
        final Timeline t = new Timeline(new KeyFrame(duration,
                new KeyValue(n.translateXProperty(), x == null ? n.getTranslateX() : (x + (mode == TranslateMode.ABSOLUTE ? 0 : n.getTranslateX()))),
                new KeyValue(n.translateYProperty(), y == null ? n.getTranslateY() : (y + (mode == TranslateMode.ABSOLUTE ? 0 : n.getTranslateY())))
        ));
        if (onFinished != null)
            t.setOnFinished(onFinished);
        animation(t);
    }

    public static void fade(Node n, Double from, Double to, Duration duration) {
        fade(n, from, to, duration, null);
    }

    public static void fade(Node n, Double from, Double to, Duration duration, EventHandler<ActionEvent> onFinished) {
        if (from != null)
            n.setOpacity(from);
        final Timeline a = new Timeline(new KeyFrame(duration, new KeyValue(n.opacityProperty(), to)));
        if (onFinished != null)
            a.setOnFinished(onFinished);
        Events.animation(a);
    }
}
