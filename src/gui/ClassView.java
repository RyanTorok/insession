package gui;

import classes.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.*;
import javafx.util.Duration;
import main.*;
import net.PostEngine;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ClassView extends TaskView {

    public static final Color DEFAULT_BACKGROUND = Color.web("#f2f1e8");

    private final ClassPd classPd;
    private HBox sideBarAndBody;
    private VBox[] sideBars;
    private Pane mainBody;
    private Pane[] bodyPanes;
    private Color textFill;
    private Color lighterTextFill;
    private Color primary;
    private Color lighter;
    private PostEngine postEngine;
    private VBox postsList;
    private boolean displayPostTextOnSidebar = false;
    private StackPane postFiltersAndList;
    private GradesBody gradesBody;
    private VBox filters;
    private Color backgroundColor = DEFAULT_BACKGROUND;
    private DateFilter dateFilter;
    private VBox mainSideBar;
    private StackPane sideBarsPane;


    private final int SIDEBAR_SCALED_WIDTH = 350;
    private int activeSubsection = -1;

    public ClassView(ClassPd classPd) {
        super(classPd.getCastOf().getName() + " - " + classPd.getTeacherLast() + " - P" + classPd.getPeriodNo());
        this.classPd = classPd;
        primary = classPd.getColor();
        lighter = Colors.highlightColor(primary).desaturate().desaturate();
        textFill = Colors.textFill(primary == null ? Color.WHITE : primary);
        lighterTextFill = Colors.textFill(lighter == null ? Color.WHITE : lighter);
    }

    public ClassView(ClassPd pd, ClassItem item) {
        this(pd);
        //TODO navigate to item
    }

    @Override
    ImageView getPlaceholderImage() {
        return null;
    }

    ScrollPane bodyPanesWrapper;

    @Override
    public Pane initDisplay() {
        //VBox titleBar = makeTitleBar();
        sideBars = makeSideBars();
        mainBody = makeMainBody();
        bodyPanes = makeBodyPanes();
        bodyPanesWrapper = new ScrollPane(mainBody);
        Styles.setBackgroundColor(bodyPanesWrapper, backgroundColor);
        bodyPanesWrapper.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        bodyPanesWrapper.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        bodyPanesWrapper.setMaxWidth(Size.width(Size.DEFAULT_WIDTH));
        bodyPanesWrapper.setFitToWidth(true);
        bodyPanesWrapper.setFitToHeight(true);
        sideBarAndBody = new HBox(makeSideBarsWrapper(), bodyPanesWrapper);
        HBox.setHgrow(bodyPanesWrapper, Priority.ALWAYS);
        sideBarAndBody.setMaxWidth(Size.width(Size.DEFAULT_WIDTH));
        Styles.setBackgroundColor(sideBarAndBody, backgroundColor);
        VBox toReturn = new VBox(sideBarAndBody);
        Styles.setBackgroundColor(toReturn, backgroundColor);
        VBox.setVgrow(sideBarAndBody, Priority.ALWAYS);
        return toReturn;
    }

    private Pane makeMainBody() {
        AutoColoredLabel header = new AutoColoredLabel("Class at a Glance", this);
        header.setFont(Font.font(Size.fontSize(24)));
        VBox recentFeedback = makeRecentFeedback(5);
        BorderPane body = new BorderPane();
        body.setTop(header);
        body.setLeft(recentFeedback);
        body.setRight(new AutoColoredLabel("dark", this) {{
            addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                toggleDarkMode();
            });
        }});
        return body;
    }

    protected void invertColors() {
        backgroundColor = Colors.invertColor(backgroundColor);
        Colors.invertGrayscale(ClassView.this);
        for (int i = 0; i < sideBars.length; i++) {
            if (i == activeSubsection)
                continue;
            Colors.invertGrayscale(sideBars[i]);
            Colors.invertGrayscale(bodyPanes[i]);
        }
    }

    private VBox makeRecentFeedback(int maxSize) {
        AutoColoredLabel header = new AutoColoredLabel("Recent Feedback", this);
        header.setFont(Font.font(Size.fontSize(18)));
        VBox pane = new VBox(header);
        pane.setSpacing(Size.height(5));
        final ArrayList<HashMap<User, StudentGrades>> gradebook = classPd.getGradebook().getGradebook();
        for (int i = gradebook.size() - 1, count = 0; i >= 0; i--) {
            final HashMap<User, StudentGrades> markingPeriod = gradebook.get(i);
            final StudentGrades studentGrades = markingPeriod.get(User.active());
            if (studentGrades == null)
                return pane;
            for (Assignment assignment : studentGrades.getGrades().descendingKeySet()) {
                final Grade grade = studentGrades.getGrades().get(assignment);
                final AutoColoredLabel assignmentName = new AutoColoredLabel(assignment.getName(), this);
                final AutoColoredLabel gradeText = new AutoColoredLabel(grade.getDisplayTextOutOf(User.active(), true), this);
                final VBox entry = new VBox(assignmentName, gradeText);
                entry.setSpacing(Size.height(5));
                Line separator = new Line(Size.width(5), 0, Size.width(200), 0);
                separator.setStrokeWidth(Size.height(2));
                separator.setStrokeLineCap(StrokeLineCap.ROUND);
                separator.setFill(Colors.textFill(backgroundColor));
                separator.setOpacity(0.5);
                pane.getChildren().addAll(entry, separator);
                count++;
                if (count >= maxSize)
                    break;
            }
        }
        //test code, remove this line
        final AutoColoredLabel assignmentName = new AutoColoredLabel("Test Assignment Stress Test Stress Test!!!!!!!!!!!!!!!!!!!!!!!!!!!!!111", this);

        final AutoColoredLabel gradeText = new AutoColoredLabel(new Grade(93.3333).getDisplayTextOutOf(User.active(), true), this);
        final VBox entry = new VBox(assignmentName, gradeText);
        entry.setSpacing(Size.height(5));
        Line separator = new Line(Size.width(5), 0, Size.width(200), 0);
        separator.setStrokeWidth(Size.height(2));
        separator.setStrokeLineCap(StrokeLineCap.ROUND);
        separator.setFill(Colors.textFill(backgroundColor));
        separator.setOpacity(0.5);
        pane.getChildren().addAll(entry, separator);
        return pane;
    }

    private VBox makeMainSideBar() {
        String[] titles = {"Discussion", "Lectures", "Files", "Assignments", "Workspaces", "Syllabus", "Grades", "People", "Groups"};
        MainSBItem[] cards = new MainSBItem[titles.length];
        for (int i = 0; i < titles.length; i++) {
            cards[i] = new MainSBItem(this, titles[i]);
            HBox.setHgrow(cards[i], Priority.ALWAYS);
        }
        mainSideBar = new VBox(cards);
        mainSideBar.setPrefWidth(Size.width(SIDEBAR_SCALED_WIDTH));
        return mainSideBar;
    }

    private HBox makeSideBarsWrapper() {
        Line border = new Line();
        border.setStroke(Color.DARKGRAY);
        border.setStartY(Size.height(50));
        border.setEndY(TaskViewWrapper.fullHeight - Size.height(60));
        border.setStrokeWidth(Size.width(2));
        border.setStrokeLineCap(StrokeLineCap.ROUND);

        final double smallWidth = Size.width(20);
        final double largeWidth = Size.width(350);

        HBox borderHitbox = new HBox(border);
        borderHitbox.setMinWidth(smallWidth);
        borderHitbox.setAlignment(Pos.CENTER);
        borderHitbox.setCursor(Cursor.E_RESIZE);
        borderHitbox.setViewOrder(1);

        sideBarsPane = new StackPane(makeMainSideBar());
        sideBarsPane.setPrefWidth(Size.width(SIDEBAR_SCALED_WIDTH));
        sideBarsPane.setPadding(Size.insets(0, 5, 0, 0));
        ScrollPane sidebarsWrapper = new ScrollPane(sideBarsPane);
        Styles.setBackgroundColor(sidebarsWrapper, backgroundColor);
        sidebarsWrapper.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        sidebarsWrapper.setHbarPolicy(ScrollBarPolicy.NEVER);
        sidebarsWrapper.setPrefWidth(largeWidth);
        sidebarsWrapper.setFitToWidth(true);

        HBox sideBarAndBorder = new HBox(sidebarsWrapper, borderHitbox);
        sideBarAndBorder.setAlignment(Pos.CENTER);
        //border drag sidebar code
        borderHitbox.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            sidebarsWrapper.setMinWidth(Math.max(sidebarsWrapper.getMinWidth() + event.getX(), 0));
            sidebarsWrapper.setPrefWidth(Math.max(sidebarsWrapper.getPrefWidth() + event.getX(), 0));
            sidebarsWrapper.setMaxWidth(Math.max(sidebarsWrapper.getMaxWidth() + event.getX(), 0));
            //emulate the click-disappear so that it re-expands when you click it.
            if (sidebarsWrapper.getPrefWidth() == 0)
                sidebarsWrapper.setVisible(false);
            else sidebarsWrapper.setVisible(true);
        });


        borderHitbox.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getClickCount() == 2) {
                if (sidebarsWrapper.isVisible()) {
                    sidebarsWrapper.setMinWidth(0);
                    sidebarsWrapper.setPrefWidth(0);
                    sidebarsWrapper.setMaxWidth(0);
                    sidebarsWrapper.setVisible(false);
                } else {
                    sidebarsWrapper.setMinWidth(largeWidth);
                    sidebarsWrapper.setPrefWidth(largeWidth);
                    sidebarsWrapper.setMaxWidth(largeWidth);
                    sidebarsWrapper.setVisible(true);
                }
            }
        });
        sidebarsWrapper.setMinWidth(largeWidth);
        sidebarsWrapper.setPrefWidth(largeWidth);
        sidebarsWrapper.setMaxWidth(largeWidth);
        return sideBarAndBorder;
    }

    private VBox[] makeSideBars() {
        VBox[] sidebars = {makePostsSB(), makeFilesSB(), makeGradesSB()};
        for (VBox sidebar : sidebars) {
            sidebar.setPrefWidth(Size.width(350));
        }
        return sidebars;
    }

    private VBox makePostsSB() {
        AutoColoredLabel newThread = new AutoColoredLabel("New Thread", this);
        Events.underlineOnMouseOver(newThread);
        newThread.setFont(Font.font(Size.fontSize(14)));
        newThread.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> ((PostsBody) bodyPanes[0]).newThread());
        AutoColoredLabel expandAllToggle = new AutoColoredLabel("Expand All", this) {

            private boolean state = false;

            {
                Events.underlineOnMouseOver(this);
                setFont(Font.font(Size.fontSize(14)));
                addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    state = !state;
                    if (state) {
                        setText("Collapse All");
                        postsList.getChildren().forEach(child -> ((PostSBItem) child).expand());
                    } else {
                        setText("Expand All");
                        postsList.getChildren().forEach(child -> ((PostSBItem) child).collapse());
                    }
                });
            }
        };

        AutoColoredLabel filterToggle = new AutoColoredLabel("Filter Results", this) {

            private boolean state = false;

            {
                Events.underlineOnMouseOver(this);
                setFont(Font.font(Size.fontSize(14)));
                addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    //TODO fix the actual problem
                    //don't load filters if there are no posts to look for, since the empty post list can't be swapped back for some reason.
                    if (postsList.getChildren().size() == 0 && !state)
                        return;
                    state = !state;
                    if (state)
                        setText("Back to Posts");
                    else setText("Filter Results");
                    swapPostFilters();
                });
            }
        };
        AutoColoredLabel divider = new AutoColoredLabel("|", this);
        divider.setFont(Font.font(Size.fontSize(14)));

        AutoColoredLabel anotherDivider = new AutoColoredLabel("|", this);
        anotherDivider.setFont(Font.font(Size.fontSize(14)));


        HBox controls = new HBox(newThread, new Layouts.Filler(), divider, new Layouts.Filler(), expandAllToggle, new Layouts.Filler(), anotherDivider, new Layouts.Filler(), filterToggle);
        controls.setPadding(Size.insets(20, 30, 10, 15));
        postEngine = classPd.getPostEngine();
        filters = makeFilters();
        postsList = makePostsList();
        postsList.setPrefHeight(filters.getHeight());
        Styles.setBackgroundColor(postsList, backgroundColor);
        Styles.setBackgroundColor(filters, backgroundColor);
        postFiltersAndList = new StackPane(filters, postsList);
        return new VBox(controls, postFiltersAndList);
    }

    VBox makePostsList() {
        List<VBox> collect = postEngine.getPosts().stream().filter(this::matchesFilter).sorted().map(this::makePostSBItem).collect(Collectors.toList());
        return new VBox(collect.toArray(new VBox[]{}));
    }

    private boolean matchesFilter(Post post) {
        AtomicBoolean b = new AtomicBoolean(true);
        filters.getChildren().forEach(node -> {
            if (node instanceof FilterBlock) {
                FilterBlock block = (FilterBlock) node;
                boolean matches = block.matches(post);
                if (!matches)
                    b.set(false);
            }
        });
        return b.get() && dateFilter.matches(post);
    }

    private VBox makeFilters() {
        List<FilterBlock> filters = new ArrayList<>();

        filters.add(new FilterBlock(this, "By Status", true,
                new Filter("Unread", PostStatus.UNREAD),
                new Filter("Updated", PostStatus.UPDATED),
                new Filter("Unanswered", PostStatus.UNANSWERED),
                new Filter("Notes", Post.Type.Note),
                new Filter("Questions", Post.Type.Question),
                new Filter("My Posts", PostStatus.MINE),
                new Filter("Liked", PostStatus.LIKED),
                new Filter("Instructor Posts", PostStatus.INSTRUCTOR),
                new Filter("Class-Visible Posts", PostStatus.PUBLIC),
                new Filter("Group-Visible Posts", PostStatus.GROUP),
                new Filter("Private Posts", PostStatus.PRIVATE)
        ));
        dateFilter = new DateFilter(event -> filters.get(0).changeEvent(this), Color.BLACK);
        List<ClassItem> items = classPd.getAssignmentsWithPostsDesc(8);
        List<Filter> assignments = items.stream().map(item -> new Filter(item.getName(), new UUID(0, 0), item.getId())).collect(Collectors.toList());
        filters.add(new FilterBlock(this, "By Assignment", true, assignments));
        VBox toReturn = new VBox();
        toReturn.getChildren().addAll(filters);
        VBox filterUI = dateFilter.get();
        filterUI.setSpacing(Size.height(5));
        toReturn.getChildren().add(filterUI);
        toReturn.setPadding(Size.insets(20, 0, 0, 10));
        toReturn.setSpacing(Size.height(20));
//        Styles.setBackgroundColor(toReturn, Color.RED);
//        toReturn.setPrefHeight(TaskViewWrapper.fullHeight);
        return toReturn;
    }

    private VBox makePostSBItem(Post post) {
        return new PostSBItem(post);
    }

    private void swapPostFilters() {
        ObservableList<Node> workingCollection = FXCollections.observableArrayList(postFiltersAndList.getChildren());
        Collections.swap(workingCollection, 0, 1);
        postFiltersAndList.getChildren().setAll(workingCollection);
        final int[] i = {0};
        postFiltersAndList.getChildren().forEach(node -> {
            if (node instanceof Pane) {
                ((Pane) node).getChildren().forEach(node1 -> node1.setFocusTraversable(i[0] == 1));
            }
            node.setFocusTraversable(i[0] == 1);
            i[0]++;
        });
        postFiltersAndList.getChildren().get(1).requestFocus();
    }

    private VBox makeFilesSB() {
        List<SidebarHotLink> linksList = classPd.getActiveSidebarHotLinks();
        return new VBox(linksList.toArray(new SidebarHotLink[0]));
    }

    private VBox makeGradesSB() {
        VBox gradesSB = new VBox();
        School.initActiveDebug(); //TODO support school serialization
        final int[] markingPd = {School.active().getSchedule().getCurrentMarkingPeriod()};
        AutoColoredLabel lArrow = new AutoColoredLabel(Character.toString((char) 0x276e), this);
        lArrow.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> shiftGradesSB(gradesSB, --markingPd[0]));
        AutoColoredLabel mpDisplay = new AutoColoredLabel("  Grading Period " + markingPd[0] + "  ", this);
        AutoColoredLabel rArrow = new AutoColoredLabel(Character.toString((char) 0x276f), this);
        rArrow.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> shiftGradesSB(gradesSB, ++markingPd[0]));
        rArrow.setVisible(false);
        Events.highlightOnMouseOver(lArrow);
        Events.highlightOnMouseOver(rArrow);
        TextFlow markingPeriodScroll = new TextFlow(lArrow, mpDisplay, rArrow);
        StudentGrades grades = classPd.getGradebook().get(markingPd[0], User.active());
        classPd.getGradebook().getCategories().forEach(cat -> gradesSB.getChildren().add(new GradesSBCategory(classPd, cat, grades, gradesBody)));
        gradesSB.getChildren().add(markingPeriodScroll);
        return gradesSB;
    }

    private void shiftGradesSB(VBox gradesSB, int newValue) {
        StudentGrades newGrades = classPd.getGradebook().get(newValue, User.active());
        Node mps = gradesSB.getChildren().remove(0);
        gradesSB.getChildren().clear();
        gradesSB.getChildren().add(mps);
        ObservableList<Node> arrowsAndHeader = ((TextFlow) mps).getChildren();
        arrowsAndHeader.get(0).setVisible(true);
        arrowsAndHeader.get(2).setVisible(true);
        if (newValue <= 1) arrowsAndHeader.get(0).setVisible(false);
        if (newValue >= School.active().getSchedule().getCurrentMarkingPeriod())
            arrowsAndHeader.get(2).setVisible(false);
        ((AutoColoredLabel) arrowsAndHeader.get(1)).setText("  Grading Period " + newValue + "  ");
        classPd.getGradebook().getCategories().forEach(cat -> gradesSB.getChildren().add(new GradesSBCategory(classPd, cat, newGrades, gradesBody)));
    }

    private Pane[] makeBodyPanes() {
        return new Pane[]{makePostsPane(), makeFilesPane(), makeGradesPane()};
    }

    private PostsBody makePostsPane() {
        return new PostsBody(this, postEngine);
    }

    private GridPane makeFilesPane() {
        return new GridPane();
    }

    private GridPane makeGradesPane() {
        return new GradesBody();
    }

    public Color getTextFill() {
        return textFill;
    }

    public ClassPd getClassPd() {
        return classPd;
    }

    public Color getLighterTextFill() {
        return lighterTextFill;
    }

    public HBox getSideBarAndBody() {
        return sideBarAndBody;
    }

    public VBox[] getSideBars() {
        return sideBars;
    }

    public Pane[] getBodyPanes() {
        return bodyPanes;
    }

    public Color getPrimary() {
        return primary;
    }

    public Color getLighter() {
        return lighter;
    }

    public PostEngine getPostEngine() {
        return postEngine;
    }

    public VBox getPostsList() {
        return postsList;
    }

    public boolean isDisplayPostTextOnSidebar() {
        return displayPostTextOnSidebar;
    }

    public StackPane getPostFiltersAndList() {
        return postFiltersAndList;
    }

    public GradesBody getGradesBody() {
        return gradesBody;
    }

    public VBox getFilters() {
        return filters;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void selectSubSection(MainSBItem choice) {
        int i = 0;
        for (Node child : mainSideBar.getChildren()) {
            if (child instanceof MainSBItem) {
                final Duration duration = Duration.millis(200);
                if (child == choice) {
                    ((MainSBItem) child).setState(MainSBItem.State.SELECTED);
                    int atomicIndex = i;
                    final double height = ((MainSBItem) child).getHeight();
                    Events.translate(child, TranslateMode.ABSOLUTE, null, height * i * -1, duration, onFinished-> {
                        Pane mouseHandler = new Pane();
                        mouseHandler.setMinHeight(height);
                        mouseHandler.setPrefHeight(height);
                        mouseHandler.setMaxHeight(height);
                        activeSubsection = atomicIndex;
                        //forward the mouse event onto the item behind it so we can exit the section
                        mouseHandler.addEventHandler(MouseEvent.ANY, event -> Events.fireMouse(child, event.getEventType()));
                        VBox sidebarWrapper = new VBox(mouseHandler, sideBars[atomicIndex]);
                        sideBarsPane.getChildren().add(1, sidebarWrapper);
                        Events.fade(sideBars[atomicIndex], 0.0, 1.0, Duration.millis(100));
                        switchBodyPanes(bodyPanes[atomicIndex]);
                    });
                } else {
                    ((MainSBItem) child).setState(MainSBItem.State.INVISIBLE);
                    Events.fade(child, null, 0.0, duration);
                }
            }
            i++;
        }
    }

    private void switchBodyPanes(Pane newBody) {
        final Duration duration = Duration.millis(100);
        final Pane oldBody = getActiveBodyPane();
        Events.fade(oldBody, 1.0, 0.0 , duration, event-> {
            setActiveBodyPane(newBody);
            Events.fade(newBody, 0.0, 1.0, Duration.millis(duration.toMillis()));
        });
    }

    private void setActiveBodyPane(Pane newBody) {
        ((ScrollPane) sideBarAndBody.getChildren().get(1)).setContent(newBody);
    }

    private Pane getActiveBodyPane() {
        try {
            return ((Pane) ((ScrollPane) sideBarAndBody.getChildren().get(1)).getContent());
        } catch (ClassCastException e) {
            //just for stability, should never get here.
            e.printStackTrace();
            return bodyPanes[0];
        }
    }

    public void returnToHome() {
        activeSubsection = -1;
        final Duration duration = Duration.millis(200);
        Events.fade(sideBarsPane.getChildren().get(1), 1.0, 0.0, duration, event -> {
            sideBarsPane.getChildren().remove(1);
            for (Node child : mainSideBar.getChildren()) {
                if (child instanceof MainSBItem) {
                    if (child.getTranslateY() != 0) {
                        Events.translate(child, TranslateMode.ABSOLUTE, null, 0.0, duration);
                    } else {
                        Events.fade(child, null, 1.0, duration);
                    }
                    ((MainSBItem) child).setState(MainSBItem.State.UNSELECTED);
                }
            }
            switchBodyPanes(mainBody);
        });

    }

    VBox getMainSidebar() {
        return mainSideBar;
    }

    public void goToClassItem(ClassItem classItem) {

    }

    class PostSBItem extends VBox {

        private Post post;
        private boolean expanded;

        public PostSBItem(Post post) {
            this.post = post;
            expanded = false;
            AutoColoredLabel text = new AutoColoredLabel(post.getTitle(), ClassView.this);
            text.setFont(Font.font("Sans Serif", FontWeight.BOLD, Font.getDefault().getSize()));
            HBox statusIcons = new HBox();

            //insert status icons
            if (post.getStatusLabels().contains(PostStatus.UNANSWERED))
                statusIcons.getChildren().add(new BoldACLabel("?"));
            else if (post.getType().equals(Post.Type.Question)) {
                if (post.getStudentAnswers().size() != 0) {
                    statusIcons.getChildren().add(new BoldACLabel(Character.toString((char) 0x1f5e9) + "s")); //0x1f5e9 is the speech bubble
                }
                if (post.getInstructorAnswer() != null) {
                    statusIcons.getChildren().add(new BoldACLabel(Character.toString((char) 0x1f5e9) + "i"));
                }
            }
            if (post.getStatusLabels().contains(PostStatus.INSTRUCTOR)) {
                statusIcons.getChildren().add(new BoldACLabel("A"));
            }
            if (post.getStatusLabels().contains(PostStatus.GROUP)) {
                statusIcons.getChildren().add(new BoldACLabel("G"));
            }
            if (post.getStatusLabels().contains(PostStatus.PRIVATE)) {
                statusIcons.getChildren().add(new BoldACLabel("P"));
            }
            statusIcons.setSpacing(Size.width(5));
            statusIcons.setPadding(Size.insets(5, 0, 0, 0));

            HBox main = new HBox(text, new Layouts.Filler(), statusIcons);

            getChildren().add(main);
            displayPostTextOnSidebar = true;
            if (displayPostTextOnSidebar) {
                Region gap = new Region();
                gap.setPrefHeight(Size.height(5));
                getChildren().add(gap);
            }
            Pane line = new Pane();
            Styles.setBackgroundColor(line, Color.DARKGRAY);
            line.setPrefHeight(Size.height(2));
            line.setPrefWidth(SIDEBAR_SCALED_WIDTH);
            setPadding(Size.insets(5, 0));
            getChildren().add(line);
            addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getButton().equals(MouseButton.SECONDARY))
                    if (isExpanded())
                        collapse();
                    else
                        expand();
                else
                    ((PostsBody) bodyPanes[0]).fire(post);
            });
            setPadding(Size.insets(10, 10, 0, 10));
            Styles.setBackgroundColor(this, backgroundColor);
            Events.highlightOnMouseOver(this);
        }

        void expand() {
            if (isExpanded())
                return;
            AutoColoredLabel author = new AutoColoredLabel(post.getIdentifier().getAuthorName(), this);
            author.setFont(Font.font(Font.getDefault().getFamily(), FontPosture.ITALIC, Size.fontSize(12)));
            AutoColoredLabel time = new AutoColoredLabel(UtilAndConstants.parseTimestamp(new Timestamp(post.getIdentifier().getTime1())), this);
            time.setFont(Font.font(Font.getDefault().getFamily(), FontPosture.ITALIC, Size.fontSize(12)));
            getChildren().add(2, new HBox(author, new Layouts.Filler(), time));
            Region gap = new Region();
            gap.setPrefHeight(Size.height(5));
            getChildren().add(3, gap);
            AutoColoredTextFlow postCondensed = new AutoColoredTextFlow(ClassView.this, post.collapseText(230));
            postCondensed.setFont(Font.font(Size.fontSize(12)));
            getChildren().add(4, postCondensed);
            Region gap2 = new Region();
            gap2.setPrefHeight(Size.height(5));
            getChildren().add(5, gap2);
            expanded = true;
        }

        void collapse() {
            if (!isExpanded())
                return;
            getChildren().remove(5);
            getChildren().remove(4);
            getChildren().remove(3);
            getChildren().remove(2);
            expanded = false;

        }

        public Post getPost() {
            return post;
        }

        public boolean isExpanded() {
            return expanded;
        }
    }

    @Override
    protected boolean isDuplicate(TaskView view) {
        return super.isDuplicate(view) && ((ClassView) view).classPd.equals(classPd);
    }

    class BoldACLabel extends AutoColoredLabel {
        BoldACLabel(String s) {
            super(s, ClassView.this);
            setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, Font.getDefault().getSize()));
        }
    }
}
