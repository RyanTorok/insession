package main;

import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class Events {

    public static void fireMouse(Node n, EventType<MouseEvent> type) {
        n.fireEvent(new MouseEvent(type, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
    }

    public static void highlightOnMouseOver(Node n) {
        if (n instanceof Text && ((Text) n).getFill() instanceof Color) {
            Color orig = (Color) ((Text) n).getFill();
            n.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> ((Text) n).setFill(Colors.highlightColor(orig)));
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
        Color newColor = Colors.highlightColor(oldColor);
        String newColorStr = Colors.colorToHex(newColor);
        String newStyle = oldStyle.replaceAll("-fx-background-color: #......", "-fx-background-color: " + newColorStr);
        n.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            n.setStyle(oldStyle);
        });
        n.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            n.setStyle(newStyle);
        });
    }

    public static void underlineOnMouseOver(Text text) {
        text.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> text.setUnderline(true));
        text.addEventHandler(MouseEvent.MOUSE_EXITED, event -> text.setUnderline(false));
    }
}
