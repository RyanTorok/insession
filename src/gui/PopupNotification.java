package gui;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import main.Events;
import main.Root;
import main.Size;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PopupNotification extends VBox {

    private TextFlow mainText;
    private HBox optionsPane;

    public PopupNotification(String text, ConfirmableTask... options) {
        mainText = new TextFlow(new Text(text));
        optionsPane = new HBox();
        optionsPane.getChildren().addAll(Arrays.stream(options).map(TaskedHBox::new).collect(Collectors.toList()));
        optionsPane.getChildren().forEach(option -> {
            Events.highlightOnMouseOver(option);
            option.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> ((TaskedHBox) option).getTask().run());
        });
        setPrefWidth(Size.width(400));
        Root.getPortal().showNotification(this);
    }

    private static class TaskedHBox extends HBox {
        //anonymous HBox which references the runnable task
        private Runnable task;

        public TaskedHBox(ConfirmableTask option) {
            super(new Text(option.getOption()));
            task = option.getTask();
        }

        public Runnable getTask() {
            return task;
        }

        public void setTask(Runnable task) {
            this.task = task;
        }
    }
}
