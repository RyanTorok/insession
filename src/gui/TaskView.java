package gui;


import javafx.scene.control.ScrollPane;

public class TaskView extends ScrollPane {

    private TaskViewWrapper wrapper;
    private boolean lockedFullScreen;

    public TaskView(String title) {
        super();
        setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
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

}