package gui;


import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import main.Root;
import main.Size;
import main.UtilAndConstants;

public abstract class TaskView extends ScrollPane {

    private TaskViewWrapper wrapper;
    private boolean lockedFullScreen;
    private HBox minimizedDisplay;
    private Pane fullDisplay;
    private boolean dragged;
    private double dy;
    private double lastClickY;

    public TaskView(String title) {
        super();
        setStyle("-fx-background-color: transparent ; -fx-background: transparent");
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

    //sets the minimized display to an image version of the screen's state before the shrink is triggered.
    private void changeMinimized(double width, double height, boolean screenshot) {
        ImageView placeholder = null;
        if (!screenshot)
            placeholder = getPlaceholderImage();
        if (!screenshot && placeholder == null)
            placeholder = new ImageView();
        minimizedDisplay = new HBox(screenshot ? new ImageView(new WritableImage(this.getContent().snapshot(new SnapshotParameters(), null).getPixelReader(), 0, 0, (int) this.getContent().getLayoutBounds().getWidth(), (int) this.getContent().getLayoutBounds().getHeight()))
            {{setPreserveRatio(true); setFitWidth(width); setFitHeight(height);}} //ImageView properties
            : placeholder) // initial case, get icon or saved placeholder, depending on TaskView subclass implementation
            {{setStyle("-fx-background-color: " + UtilAndConstants.colorToHex(Root.getActiveUser().getAccentColor())); setAlignment(screenshot ? Pos.TOP_LEFT : Pos.CENTER);}}; //HBox (with one element) properties

        minimizedDisplay.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            TaskView view = TaskView.this;
            if (wrapper.getState() == TaskViewWrapper.STACK_STATE) {
                view.setDragged(true);
                int index = wrapper.getActiveViews().indexOf(view);
                int diff = wrapper.getWhich() - index;
                double dy = event.getY() - view.getLastClickY();
                view.setTranslateY(view.getDy() + dy);
                view.setDy(view.getDy() + dy);
            }
        });

        setContent(minimizedDisplay);
    }

    void initMinimizedDisplay() {
        changeMinimized(TaskViewWrapper.smallWidth, TaskViewWrapper.smallHeight, false);
    }

    void collapse() {
        changeMinimized(TaskViewWrapper.fullWidth, TaskViewWrapper.fullHeight, true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.NEVER);
    }

    //resets the display to the actual contents.
    void expand() {
        if (fullDisplay == null) {
            fullDisplay = new GridPane() {{
                getChildren().add(new Text("This is a test") {{
                    setFont(Font.font(30));
                }});
                setStyle("-fx-background-color: white");
                setPrefSize(Root.getPortal().getMainArea().getLayoutBounds().getWidth(), Root.getPortal().getMainArea().getLayoutBounds().getHeight() - Root.getPortal().getTop_bar().getLayoutBounds().getHeight() + Size.height(10));
            }};
            GridPane.setConstraints(fullDisplay.getChildren().get(0), 0, 0);
        }
        setContent(fullDisplay);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
    }

    public HBox getMinimizedDisplay() {
        if (minimizedDisplay == null)
            return new HBox(new ImageView() {{setImage(null); setFitWidth(TaskViewWrapper.smallWidth); setFitHeight(TaskViewWrapper.smallHeight);}});
        return minimizedDisplay;
    }

    abstract ImageView getPlaceholderImage();

    public void setDragged(boolean dragged) {
        this.dragged = dragged;
    }

    public boolean isDragged() {
        return dragged;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    public double getDy() {
        return dy;
    }

    public void setWrapper(TaskViewWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public double getLastClickY() {
        return lastClickY;
    }

    public void setLastClickY(double lastClickY) {
        this.lastClickY = lastClickY;
    }
}