package gui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import main.Events;
import main.Root;
import main.Size;

import java.util.ArrayList;

public class TaskViewWrapper extends StackPane {

    public static final int SCROLL_MILLIS = 400;
    private ArrayList<TaskView> activeViews;
    private boolean changeLock;
    private int which;
    long lastShift = 1;
    private int shiftCount;
    private EventHandler<MouseEvent> drag;
    private TaskView lastViewed, lastClosed;
    private long lastAutoScroll = 1;
    private long lastStackTransition;
    private long lastStackTransitionLength;

    public TaskViewWrapper() {
        activeViews = new ArrayList<>();
        changeLock = false;
        which = -1;
        addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getTarget() == this) {
                boolean anyDragged = false;
                for (TaskView v : activeViews) {
                    if (v.isDragged()) {
                        anyDragged = true;
                    }
                }
                if (!anyDragged)
                    Root.getPortal().hideTaskViews();
            }
        });
        Root.getPortal().getPrimaryStage().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (Root.getPortal().getState() != Main.BASE_STATE)
                return;
            if (event.getCode().equals(KeyCode.SHIFT)) {
                //double shift for stack active views
                Long now = System.currentTimeMillis();
                if (now - lastShift < 500) {
                    if (shiftCount != 2) {
                        if (state == STACK_STATE && !Root.getPortal().isHomeScreen()) {
                            lastShift = 1;
                            shiftCount = 2;
                            if (lastViewed != null)
                                select(lastViewed);
                            else if (lastClosed != null) {
                                select(lastClosed);
                            }
                        } else {
                            shiftCount = 1;
                            Root.getPortal().showTaskViews();
                            stack();
                            //used to overcome freezing from previous mouse click
                            scroll(which);
                        }
                    }
                } else {
                    shiftCount = 0;
                }
                lastShift = now;
            }
            if (Root.getPortal().isHomeScreen()) {
                return;
            }
            switch (event.getCode()) {
                case LEFT:
                    if (which > 0 && state == STACK_STATE) {
                        scroll(event.isShiftDown() ? 0 : which - 1);
                    }
                    break;
                case RIGHT:
                    if (which < activeViews.size() - 1 && state == STACK_STATE) {
                        scroll(event.isShiftDown() ? activeViews.size() - 1 : which + 1);
                    }
                    break;
                case ENTER: {
                    if (state == STACK_STATE) {
                        select(which);
                    }
                    break;
                }
                case NUMPAD1:
                case DIGIT1: scroll(Math.min(activeViews.size() - 1, 0)); break;
                case NUMPAD2:
                case DIGIT2: scroll(Math.min(activeViews.size() - 1, 1)); break;
                case NUMPAD3:
                case DIGIT3: scroll(Math.min(activeViews.size() - 1, 2)); break;
                case NUMPAD4:
                case DIGIT4: scroll(Math.min(activeViews.size() - 1, 3)); break;
                case NUMPAD5:
                case DIGIT5: scroll(Math.min(activeViews.size() - 1, 4)); break;
                case NUMPAD6:
                case DIGIT6: scroll(Math.min(activeViews.size() - 1, 5)); break;
                case NUMPAD7:
                case DIGIT7: scroll(Math.min(activeViews.size() - 1, 6)); break;
                case NUMPAD8:
                case DIGIT8: scroll(Math.min(activeViews.size() - 1, 7)); break;
                case NUMPAD9:
                case DIGIT9: scroll(Math.min(activeViews.size() - 1, 8)); break;
                case NUMPAD0:
                case DIGIT0: scroll(Math.min(activeViews.size() - 1, 9)); break;
            }
        });
    }

    static final int centerLocX = 0;
    static final double fullWidth = Size.width(1860);
    static final double fullHeight = Size.height(937);
    static final double smallWidth = fullWidth / 2;
    static final double smallHeight = fullHeight / 2;

    public static final int BASE_STATE  = 0;
    public static final int TILE_STATE  = 1;
    public static final int STACK_STATE = 2;

    private int state;

    private void selectIfStacked(int index) {
        if (state == STACK_STATE) {
            select(index);
        }
    }

    public void select(TaskView view) {
        int index = activeViews.indexOf(view);
        if (index != -1)
            select(index);
    }

    public void select(int index) {
        if (changeLock || index >= activeViews.size())
            return;
        int millis = 200;
        if (state == BASE_STATE) {
            stack();
        }
        int orig = which;
        if (lastClosed != null && lastClosed != activeViews.get(index)) {
            lastViewed = lastClosed;
        }
        long manualScrollDelay = Math.max(0, SCROLL_MILLIS - (System.currentTimeMillis() - lastAutoScroll));
        Timeline delay =  new Timeline(new KeyFrame(Duration.millis((orig == index ? 0 : 400) + manualScrollDelay)));
        delay.setOnFinished(event -> {
            scroll(index);
            stackTileSlideOut(millis);
        });
        delay.play();
        which = index;
        state = BASE_STATE;
    }

    public void stack() {
        if (state != BASE_STATE || changeLock)
            return;
        int millis = 200;
        if (which != -1)
            lastClosed = activeViews.get(which);
        shrinkActive(millis);
        Timeline delay = new Timeline(new KeyFrame(Duration.millis(millis)));
        delay.setOnFinished(event -> stackTileSlideIn(millis));
        delay.play();
        state = STACK_STATE;
    }

    private void stackTileSlideIn(int millis) {
        lastAutoScroll = System.currentTimeMillis();
        if (which == -1)
            return;
        int index = which;
        double stretch = Size.width(200);
        for (int i = 0; i < index; i++) {
            int distance = index - i;
            double offset = stretch * Math.sqrt(distance);
            Timeline comeFromLeft = new Timeline(new KeyFrame(Duration.millis(millis), new KeyValue(activeViews.get(i).translateXProperty(), centerLocX - offset)));
            comeFromLeft.play();
        }
        TaskView me = activeViews.get(index);
        Timeline centerMe = new Timeline(new KeyFrame(Duration.millis(millis), new KeyValue(me.translateXProperty(), centerLocX)));
        centerMe.play();
        for (int i = index + 1; i < activeViews.size(); i++) {
            int distance = i - index;
            double offset = stretch * Math.sqrt(distance);
            Timeline comeFromRight = new Timeline(new KeyFrame(Duration.millis(millis), new KeyValue(activeViews.get(i).translateXProperty(), centerLocX + offset)));
            comeFromRight.play();
        }
        //swap z positions
        ArrayList<TaskView> zIndexTable = new ArrayList<>();
        int i = 0, j = activeViews.size() - 1;
        while (i <= j) {
            if (index - i > j - index)
                zIndexTable.add(activeViews.get(i++));
            else zIndexTable.add(activeViews.get(j--));
        }
        getChildren().setAll(zIndexTable);
    }

    private void stackTileSlideOut(int millis) {
        lastAutoScroll = System.currentTimeMillis();
        if (which == -1)
            return;
        int index = which;
        for (int i = 0; i < index; i++) {
            Timeline goLeft = new Timeline(new KeyFrame(Duration.millis(millis), new KeyValue(activeViews.get(i).translateXProperty(), Size.width(-2000))));
            goLeft.play();
        }
        TaskView me = activeViews.get(index);
        growView(me, millis);
        for (int i = index + 1; i < activeViews.size(); i++) {
            Timeline goRight = new Timeline(new KeyFrame(Duration.millis(millis), new KeyValue(activeViews.get(i).translateXProperty(), Size.width(2000))));
            goRight.play();
        }
    }

    private void shrinkActive(int millis) {
        if (which == -1)
            return;
        TaskView me = activeViews.get(which);
        shrinkView(me, millis);
    }

    private void shrinkView(TaskView me, int millis) {
        changeSize(me, millis, centerLocX, 0, smallWidth, smallHeight, false, false);
    }

    private void growView(TaskView me, int millis) {
        changeSize(me, millis, 0, Size.height(-5), fullWidth, fullHeight, true, false);
    }

    private void initView(TaskView me) {
        changeSize(me, 1, centerLocX, 0, smallWidth, smallHeight, false, true);
    }

    private void changeSize(TaskView me, int millis, double toX, double toY, double newWidth, double newHeight, boolean modifiableAfter, boolean initial) {
        if (me.getContent() == null)
            me.initMinimizedDisplay();
        if (!initial && !modifiableAfter)
            me.collapse();
        Timeline change = new Timeline(new KeyFrame(Duration.millis(millis),
                new KeyValue(me.translateXProperty(), toX),
                new KeyValue(me.translateYProperty(), toY),
                new KeyValue(me.maxWidthProperty(), newWidth),
                new KeyValue(me.prefWidthProperty(), newWidth),
                new KeyValue(me.minWidthProperty(), newWidth),
                new KeyValue(me.maxHeightProperty(), newHeight),
                new KeyValue(me.prefHeightProperty(), newHeight),
                new KeyValue(me.minHeightProperty(), newHeight),
                new KeyValue(((ImageView) ((HBox) me.getContent()).getChildren().get(0)).fitHeightProperty(), newHeight),
                new KeyValue(((ImageView) ((HBox) me.getContent()).getChildren().get(0)).fitWidthProperty(), newWidth)));
        if (!initial && modifiableAfter) {
            change.setOnFinished(event -> me.expand());
        }
        change.play();
        lastStackTransition = System.currentTimeMillis();
        lastStackTransitionLength = millis;
    }

    public void tile() {
        if (state != BASE_STATE || changeLock)
            return;
    }

    public void vsplit(TaskView toSplit) {

    }

    public void vsplit(int index) {
        if (getActiveViews().size() <= index || index < 0)
            return;
        vsplit(getActiveViews().get(index));
    }

    public void hsplit(TaskView toSplit) {

    }

    public void hsplit(int index) {
        if (getActiveViews().size() <= index || index < 0)
            return;
        hsplit(getActiveViews().get(index));
    }

    public void fullscreen(TaskView view) {
        fullscreen(activeViews.indexOf(view));
    }

    public void fullscreen(int index) {
        if (getActiveViews().size() <= index || index < 0)
            return;
        if (which != index) {
            select(index);
        }
        Root.getPortal().getPrimaryStage().setFullScreen(true);
    }


    public void restoreDown(TaskView view) {
        if (view.isLockedFullScreen()) {
            return;
        }
        Root.getPortal().getPrimaryStage().setFullScreen(false);

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

    public void launch(TaskView view) {
        int millis = 200;
        view.setWrapper(this);
        view.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (state == STACK_STATE && !view.isDragged()) {
                int indexOf = activeViews.indexOf(view);
                if (which == indexOf)
                    select(view);
                else
                    scroll(activeViews.indexOf(view));
            }
            view.setDragged(false);
        });

        view.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> view.setLastClickY(event.getY()));

        view.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if (state == STACK_STATE && view.isDragged()) {
                double dy = view.getDy();
                double endPos = Size.height(dy < Size.height(-400) ? -1500 : dy > Size.height(400) ? 1500 : 0);
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(200), new KeyValue(view.translateYProperty(), endPos)));
                timeline.play();
                if (endPos != 0) {
                    removeView(view);
                    timeline.setOnFinished(event1 -> getChildren().remove(view));
                }
            }
        });
        lastClosed = lastViewed = current();
        this.getActiveViews().add(view);
        this.getChildren().add(view);
        view.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        view.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        initView(view);
        stack();
        scroll(getActiveViews().size() - 1);
        view.setTranslateY(Size.height(2000));
        Timeline initNew = new Timeline(new KeyFrame(Duration.millis(1), new KeyValue(view.translateYProperty(), Size.height(2000))));
        initNew.setOnFinished(event -> {
            Timeline delay = new Timeline(new KeyFrame(Duration.millis(millis)));
            delay.setOnFinished(event1 -> {
                Timeline riseUp = new Timeline(new KeyFrame(Duration.millis(millis * 2), new KeyValue(view.translateYProperty(), 0)));
                riseUp.setOnFinished(event2 -> {
                    Timeline delay2 = new Timeline(new KeyFrame(Duration.millis(millis * 2)));
                    delay2.setOnFinished(event3 -> {
                        view.initialize();
                        which = activeViews.size() - 1;
                        stackTileSlideOut(millis / 2);
                    });
                    delay2.play();
                });
                riseUp.play();
            });
            delay.play();
        });
        initNew.play();
        state = BASE_STATE;
    }

    private void removeView(TaskView view) {
        int oldIndex = activeViews.indexOf(view);
        int newIndex = which >= oldIndex && which > 0 ? which - 1 : which;
        activeViews.remove(view);
        if (activeViews.size() == 0) Root.getPortal().hideTaskViews();
        else scroll(newIndex);
    }

    public void close(TaskView view) {
        int origState = state;
        if (state == BASE_STATE)
            stack();
        activeViews.remove(view);
        if (which == activeViews.size())
            which--;
        stackTileSlideIn(400);
        if (origState == BASE_STATE)
            select(which);
    }

    public void close (int index) {
        if (index < 0 || index >= activeViews.size())
            return;
        close(activeViews.get(index));
    }

    private void scroll(int index) {
        if (state != STACK_STATE || index >= activeViews.size())
            return;
        which = index;
        Timeline delay = new Timeline(new KeyFrame(Duration.millis(Math.max(0, (lastStackTransition + lastStackTransitionLength) - System.currentTimeMillis()))));
        delay.setOnFinished(event -> {
            stackTileSlideIn(SCROLL_MILLIS);
            Root.getPortal().getSubtitle().setText(activeViews.get(index).getTitle());
        });
        delay.play();
    }

    public TaskView current() {
        if (which < 0 || which >= activeViews.size())
            return null;
        return activeViews.get(which);
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getWhich() {
        return which;
    }

    void autoCloseTaskView(TaskView view) {
        int index = activeViews.indexOf(view);
        if (index == -1)
            return;
        stack();
        view.setDy(-401);
        view.setDragged(true);
        Events.fireMouse(view, MouseEvent.MOUSE_RELEASED);
    }
}
