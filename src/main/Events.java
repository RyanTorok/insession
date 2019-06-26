package main;

import gui.Styles;
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
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

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

    public static void fireMouse(Node n, EventType<? extends MouseEvent> type) {
        n.fireEvent(new MouseEvent(type, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
    }

    public static void highlightOnMouseOver(Node n, Color... target) {
        n.setCursor(Cursor.CLOSED_HAND);
        Color defaultColor = Styles.getBackgroundColor(n);
        if (defaultColor == null)
            defaultColor = Color.TRANSPARENT;
        Color finalDefaultColor = defaultColor;
        AtomicReference<Color> orig = new AtomicReference<>(defaultColor);
        if (n instanceof Text && ((Text) n).getFill() instanceof Color) {
            n.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
                orig.set(((Color) ((Text) n).getFill()));
                ((Text) n).setFill(target.length == 0 ? Colors.highlightColor(orig.get()) : target[0]);
            });
            n.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
                Color newColor = orig.get();
                if (newColor == null)
                    newColor = finalDefaultColor;
                ((Text) n).setFill(newColor);
            });
            return;
        }
        AtomicReference<Boolean> firstEnter = new AtomicReference<>(false);
        if (n instanceof Label && ((Label) n).getTextFill() instanceof Color) {
            n.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
                firstEnter.set(true);
                final Paint textFill = ((Label) n).getTextFill();
                if (textFill instanceof Color)
                    orig.set((Color) textFill);
                else orig.set(finalDefaultColor);
                ((Label) n).setTextFill(target.length == 0 ? Colors.highlightColor(orig.get()) : target[0]);
            });
            n.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
                if (!firstEnter.get())
                    return;
                Color newColor = orig.get();
                if (newColor == null)
                    newColor = finalDefaultColor;
                ((Label) n).setTextFill(newColor);
            });
            return;
        }
        n.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            firstEnter.set(true);
            Color old = Styles.getBackgroundColor(n);
            orig.set(old);
            CornerRadii radius = CornerRadii.EMPTY;
            if (n instanceof Region && ((Region) n).getBackground() != null) {
                radius = ((Region) n).getBackground().getFills().get(0).getRadii();
            }
            if (old != null || target.length > 0 && target[0] != null)
                Styles.setBackgroundColor(n, target.length == 0 ? Colors.highlightColor(old) : target[0], radius);
        });
        n.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            if (!firstEnter.get())
                return;
            Color old = Styles.getBackgroundColor(n);
            CornerRadii radius = CornerRadii.EMPTY;
            if (n instanceof Region && ((Region) n).getBackground() != null) {
                radius = ((Region) n).getBackground().getFills().get(0).getRadii();
            }
            if (old != null || target.length > 0 && target[0] != null)
                Styles.setBackgroundColor(n, orig.get(), radius);
        });
    }

    public static void underlineOnMouseOver(Text text) {
        text.setCursor(Cursor.CLOSED_HAND);
        text.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> text.setUnderline(true));
        text.addEventHandler(MouseEvent.MOUSE_EXITED, event -> text.setUnderline(false));
    }

    public static void underlineOnMouseOver(Label text) {
        text.setCursor(Cursor.CLOSED_HAND);
        text.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> text.setUnderline(true));
        text.addEventHandler(MouseEvent.MOUSE_EXITED, event -> text.setUnderline(false));
    }


    /* TODO

    public static void hoverPaneOnMouseOver(Node target, Pane hover) {
        AnchorPane hoverPane = new AnchorPane(hover);
        target.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            Root.getPortal().getMainArea().getChildren().add(hoverPane);
        });
    }

    //0: both, 1: left, 2: right
    public static void hoverPaneOnMouseClick(Node target, Pane hover, int button) {

    }
    */
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

    public static void translate(Node n, TranslateMode mode, Double x,    Double y, Duration duration) {
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
