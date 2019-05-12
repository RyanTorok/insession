package main;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Layouts {

    public static void expandText(Text t) {
        Parent parent_ = t.getParent();
        if (!(parent_ instanceof Pane)) {
            throw new IllegalArgumentException("Node does not have parent with modifiable children");
        }
        Pane parent = ((Pane) parent_);
        int indexInChild = parent.getChildren().indexOf(t);
        if (t instanceof TruncatedText)
            ((TruncatedText) t).expand();
        parent.getChildren().set(indexInChild, new TextFlow(t));
    }

    public static void collapseText(Text t, Pane parent) {
        for (int i = 0; i < parent.getChildren().size(); i++) {
            final Node flow = parent.getChildren().get(i);
            if (flow instanceof TextFlow) {
                if (((TextFlow) flow).getChildren().contains(t)) {
                    parent.getChildren().set(i, t);
                    if (t instanceof TruncatedText)
                        ((TruncatedText) t).collapse();
                    return;
                }
            }
        }
    }

    public static class Filler extends Region {
        public Filler() {
            HBox.setHgrow(this, Priority.ALWAYS);
            VBox.setVgrow(this, Priority.ALWAYS);
        }
    }


    //returns a copy of the argument string with all newline characters replaced by spaces
    public static String oneLineString(String s) {
        return s.replaceAll("\n", " ");
    }

}
