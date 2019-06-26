package gui;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import main.Events;
import main.Size;

public class TaskViewsSidePane extends VBox {
    public TaskViewsSidePane() {
        Styles.setBackgroundColor(this, Color.DARKGRAY);

        //Layout selection row
        HBox layoutSelectors = new HBox();
        LayoutSelector fullScreen = new LayoutSelector(false, false, false);
        LayoutSelector hSplit = new LayoutSelector(false, true, false);
        LayoutSelector vSplit = new LayoutSelector(true, false, false);
        LayoutSelector left1right2 = new LayoutSelector(true, false, true);
        LayoutSelector left2right1 = new LayoutSelector(true, true, false);
        LayoutSelector left2right2 = new LayoutSelector(true, true, true);
        layoutSelectors.getChildren().addAll(fullScreen, hSplit, vSplit, left1right2, left2right1, left2right2);
        layoutSelectors.setSpacing(Size.width(10));

        getChildren().add(layoutSelectors);

    }

    private static void selectLayout(boolean vSplit, boolean hSplitLeft, boolean hSplitRight) {

    }

    private static class LayoutSelector extends HBox {
        public LayoutSelector(boolean vSplit, boolean hSplitLeft, boolean hSplitRight) {
            VBox left = new VBox();
            left.setSpacing(Size.height(3));
            fillSideAndAdd(left, hSplitLeft);
            if (vSplit) {
                VBox right = new VBox();
                fillSideAndAdd(right, hSplitRight);
            }
            setSpacing(Size.width(3));
            setPrefSize(Size.width(20), Size.height(20));
            Styles.setBackgroundColor(this, Color.DARKGRAY, new CornerRadii(8));
            Events.highlightOnMouseOver(this);
            addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                selectLayout(vSplit, hSplitLeft, hSplitRight);
            });
        }


        private void fillSideAndAdd(VBox toFill, boolean hSplit) {
            toFill.setSpacing(Size.height(3));
            toFill.getChildren().add(new RectFiller());
            if (hSplit)
                toFill.getChildren().add(new RectFiller());
            getChildren().add(toFill);
        }
    }

    private static class RectFiller extends Rectangle {
        RectFiller() {
            Styles.setBackgroundColor(this, Color.WHITE);
            HBox.setHgrow(this, Priority.ALWAYS);
            VBox.setVgrow(this, Priority.ALWAYS);
        }
    }
}
