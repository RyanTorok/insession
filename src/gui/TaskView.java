package gui;


import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import main.Colors;
import main.Root;
import main.User;

public abstract class TaskView extends ScrollPane implements ColorThemeBase {

    private String title;
    private TaskViewWrapper wrapper;
    private boolean lockedFullScreen;
    private HBox minimizedDisplay;
    private Pane fullDisplay;
    private boolean dragged;
    private double dy;
    private double lastClickY;
    private long fileId = -1;
    private boolean initializeFlag = true;
    private boolean darkMode = false;

    public TaskView(String title) {
        super();
        this.title = title;
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

        if (screenshot) {
            final ImageView imageView = new ImageView(new WritableImage(this.getContent().snapshot(new SnapshotParameters(), null).getPixelReader(), 0, 0,
                    (int) this.getContent().getLayoutBounds().getWidth(), (int) this.getContent().getLayoutBounds().getHeight()));
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            minimizedDisplay = new HBox(imageView);
        } else {
            minimizedDisplay = new HBox(placeholder);
        }
        minimizedDisplay .setAlignment(screenshot ? Pos.TOP_LEFT : Pos.CENTER);

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
        Styles.setBackgroundColor(this, Color.TRANSPARENT);
        changeMinimized(TaskViewWrapper.fullWidth, TaskViewWrapper.fullHeight, true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.NEVER);
    }

    //resets the display to the actual contents.
    void expand() {
       // if (fullDisplay == null) initialize();
        setContent(fullDisplay);
        Color contentBackground = Styles.getBackgroundColor(fullDisplay);
        if (contentBackground != null)
            Styles.setBackgroundColor(this, contentBackground);
        else Styles.setBackgroundColor(this, Color.WHITE);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        Root.getPortal().getSubtitle().setText(title);
    }

    protected void initialize() {
        fullDisplay = initDisplay();
        fullDisplay.setPrefSize(Root.getPortal().getMainArea().getLayoutBounds().getWidth(), Root.getPortal().getMainArea().getLayoutBounds().getHeight() - Root.getPortal().getTopbar().getLayoutBounds().getHeight());
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    protected abstract Pane initDisplay();

    protected boolean isDuplicate(TaskView view) {
        return view.getClass().equals(this.getClass());
    }

    public Pane getFullDisplay() {
        return fullDisplay;
    }

    public void setFullDisplay(Pane fullDisplay) {
        this.fullDisplay = fullDisplay;
    }

    public void setInitializeFlag(boolean initializeFlag) {
        this.initializeFlag = initializeFlag;
    }

    public boolean isInitializeFlag() {
        return initializeFlag;
    }

    public boolean getInitializeFlag() {
        return initializeFlag;
    }

    @Override
    public boolean isDarkMode() {
        return darkMode;
    }

    protected final void toggleDarkMode() {
        darkMode = !darkMode;
        invertColors();
    }

    protected void invertColors() {
        Colors.invertGrayscale(this);
    }
}