package gui;


import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import main.Root;

import java.util.Random;

public class TaskView extends ScrollPane {

    private TaskViewWrapper wrapper;
    private boolean lockedFullScreen;
    private TaskViewContents onMinimized;
    private TaskViewContents onFullScreen;

    public TaskView(String title) {
        super();
        setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        int x = new Random().nextInt(10);
        AnchorPane test = new AnchorPane();
        test.setStyle("-fx-background-color: #" + x + "00000");
        test.setPrefSize(Root.width(400), Root.height(400));
        test.getChildren().add(new Text("test"));
        getChildren().add(test);
    }


    public boolean isLockedFullScreen() {
        return lockedFullScreen;
    }

    public void setLockedFullScreen(boolean lockedFullScreen) {
        this.lockedFullScreen = lockedFullScreen;
    }

    public void lockToTest() {
        setLockedFullScreen(true);
        getWrapper().fullscreen(this);
        getWrapper().setChangeLock(true);
    }

    public void unlockFromTest() {
        setLockedFullScreen(false);
        getWrapper().restoreDown(this);
        getWrapper().setChangeLock(false);
    }

    public TaskViewWrapper getWrapper() {
        return wrapper;
    }

    public TaskViewContents getOnFullScreen() {
        return onFullScreen;
    }

    public TaskViewContents getOnMinimized() {
        return onMinimized;
    }
}