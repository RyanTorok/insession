package gui;

import java.util.ArrayList;

public class TaskViewWrapper {
    private ArrayList<TaskView> activeViews;
    private boolean changeLock;


    public void tile() {

    }

    public void stack() {

    }

    public void vsplit(TaskView toSplit) {

    }

    public void vsplit(int index) {
        if (getActiveViews().size() >= index)
            return;
        vsplit(getActiveViews().get(index));
    }

    public void hsplit(TaskView toSplit) {

    }

    public void hsplit(int index) {
        if (getActiveViews().size() >= index)
            return;
        hsplit(getActiveViews().get(index));
    }

    public void fullscreen(TaskView view) {

    }

    public void fullscreen(int index) {
        if (getActiveViews().size() >= index)
            return;
        fullscreen(getActiveViews().get(index));
    }


    public void restoreDown(TaskView view) {
        if (view.isLockedFullScreen()) {
            return;
        }
    }

    public void restoreDown(int index) {
        if (getActiveViews().size() >= index)
            return;
        restoreDown(getActiveViews().get(index));
    }

    public ArrayList<TaskView> getActiveViews() {
        return activeViews;
    }

    public void setActiveViews(ArrayList<TaskView> activeViews) {
        this.activeViews = activeViews;
    }

    public boolean isChangeLock() {
        return changeLock;
    }

    public void setChangeLock(boolean changeLock) {
        this.changeLock = changeLock;
    }
}
