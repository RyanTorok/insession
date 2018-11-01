package gui;

import classes.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.*;
import main.*;
import net.PostEngine;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ClassView extends TaskView {

    private final ClassPd classPd;
    private HBox sideBarAndBody;
    private VBox[] sideBars;
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
    private Color backgroundColor = Color.web("#e6e8f2");
    private DateFilter dateFilter;

    public ClassView(ClassPd classPd) {
        super(classPd.getCastOf().getName() + " - P" + classPd.getPeriodNo() + " - " + classPd.getTeacherLast());
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

    @Override
    public Pane initDisplay() {
        VBox titleBar = makeTitleBar();
        sideBars = makeSideBars();
        bodyPanes = makeBodyPanes();
        sideBarAndBody = new HBox(makeSideBarsWrapper(), bodyPanes[0]);
        sideBarAndBody.setHgrow(bodyPanes[0], Priority.ALWAYS);
        Styles.setBackgroundColor(sideBarAndBody, backgroundColor);
        VBox toReturn = new VBox(titleBar, sideBarAndBody);
        toReturn.setVgrow(sideBarAndBody, Priority.ALWAYS);
        return toReturn;
    }

    private VBox makeTitleBar() {
        HBox titleAndControls = new HBox();

        Tab posts = new Tab(0, "posts"),
                files = new Tab(1, "files"),
                grades = new Tab(2, "assignments and grades");

        HBox tabs = new HBox(posts, files, grades);
        tabs.setSpacing(Size.width(20));
        VBox toReturn = new VBox(titleAndControls, tabs);
        Styles.setBackgroundColor(toReturn, backgroundColor);
        return toReturn;
    }

    private HBox makeSideBarsWrapper() {
        Line border = new Line();
        border.setStroke(Color.DARKGRAY);
        border.setStartY(Size.height(50));
        border.setEndY(TaskViewWrapper.fullHeight - Size.height(60));
        border.setStrokeWidth(Size.width(2));
        border.setStrokeLineCap(StrokeLineCap.ROUND);
        HBox toReturn = new HBox(sideBars[0], border);
        toReturn.setAlignment(Pos.CENTER);
        return toReturn;
    }

    private VBox[] makeSideBars() {
        VBox[] sidebars = {makePostsSB(), makeFilesSB(), makeGradesSB()};
        for (VBox sidebar : sidebars) {
            sidebar.setPrefWidth(Size.width(350));
        }
        return sidebars;
    }

    private VBox makePostsSB() {
        Text newThread = new Text("New Thread") {{
            setFill(Colors.textFill(backgroundColor));
            Events.underlineOnMouseOver(this);
            setFont(Font.font(Size.fontSize(14)));
            addEventHandler(MouseEvent.MOUSE_CLICKED, event -> ((PostsBody) bodyPanes[0]).newThread());
        }};
        Text expandAllToggle = new Text("Expand All") {

            private boolean state = false;

            {
                setFill(Colors.textFill(backgroundColor));
                Events.underlineOnMouseOver(this);
                setFont(Font.font(Size.fontSize(14)));
                addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    state = !state;
                    if (state) {
                        setText("Collapse All");
                        postsList.getChildren().forEach(child -> ((PostSBItem) child).expand());
                    }
                    else {
                        setText("Expand All");
                        postsList.getChildren().forEach(child -> ((PostSBItem) child).collapse());
                    }
                });
            }
        };

        Text filterToggle = new Text("Filter Results") {

            private boolean state = false;

            {
                setFill(Colors.textFill(backgroundColor));
                Events.underlineOnMouseOver(this);
                setFont(Font.font(Size.fontSize(14)));
                addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    state = !state;
                    if (state)
                        setText("Back to Posts");
                    else setText("Filter Results");
                    swapPostFilters();
                });
            }
        };
        Text divider = new Text("|");
        divider.setFill(Colors.textFill(backgroundColor));
        divider.setFont(Font.font(Size.fontSize(14)));

        Text anotherDivider = new Text("|");
        anotherDivider.setFill(Colors.textFill(backgroundColor));
        anotherDivider.setFont(Font.font(Size.fontSize(14)));


        HBox controls = new HBox(newThread, new Layouts.Filler(), divider, new Layouts.Filler(), expandAllToggle, new Layouts.Filler(), anotherDivider, new Layouts.Filler(), filterToggle);
        controls.setPadding(Size.insets(20, 35, 10, 20));
        postEngine = classPd.getPostEngine();
        filters = makeFilters();
        postsList = makePostsList();
        postFiltersAndList = new StackPane(filters, postsList);
        return new VBox(controls, postFiltersAndList);
    }

    VBox makePostsList() {
        List<VBox> collect = postEngine.getPosts().stream().filter(this::matchesFilter).sorted().map(this::makePostSBItem).collect(Collectors.toList());
        VBox toReturn = new VBox(collect.toArray(new VBox[]{}));
        toReturn.setPrefHeight(TaskViewWrapper.fullHeight);
        Styles.setBackgroundColor(toReturn, backgroundColor);
        return toReturn;
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
                new Filter("Teacher Posts", PostStatus.INSTRUCTOR),
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
        Styles.setBackgroundColor(toReturn, backgroundColor);
        toReturn.setPrefHeight(TaskViewWrapper.fullHeight);
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
        Text lArrow = new Text(Character.toString((char) 0x276e));
        lArrow.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> shiftGradesSB(gradesSB, --markingPd[0]));
        Text mpDisplay = new Text("  Grading Period " + markingPd[0] + "  ");
        Text rArrow = new Text(Character.toString((char) 0x276f));
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
        if (newValue >= School.active().getSchedule().getCurrentMarkingPeriod()) arrowsAndHeader.get(2).setVisible(false);
        ((Text) arrowsAndHeader.get(1)).setText("  Grading Period " + newValue + "  ");
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

    private class Tab extends HBox {

        private int index;
        private Text text;

        Tab(int index, String text) {
            this.index = index;
            this.text = new Text(text);
            this.text.setFont(Font.font(Size.fontSize(16)));
            getChildren().add(this.text);
            Events.underlineOnMouseOver(this.text);
            this.text.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> switchPane(index));

        }
    }

    private void switchPane(int index) {
        ObservableList<Node> children = sideBarAndBody.getChildren();
        ((Pane) children.get(0)).getChildren().set(0, sideBars[index]);
        children.set(1, bodyPanes[index]);
    }

    class PostSBItem extends VBox {

        private Post post;
        private boolean expanded;

        public PostSBItem(Post post) {
            this.post = post;
            expanded = false;
            Text text = new Text(post.getTitle());
            text.setFont(Font.font("Sans Serif", FontWeight.BOLD, Font.getDefault().getSize()));
            HBox main = new HBox(text, new Layouts.Filler());
            main.getChildren().add(new HBox() {
                {
                //insert status icons
                if (post.getStatusLabels().contains(PostStatus.UNANSWERED))
                    getChildren().add(new ColorText("?"));
                else if (post.getType().equals(Post.Type.Question)) {
                    if (post.getStudentAnswers().size() != 0) {
                        getChildren().add(new ColorText(Character.toString((char) 0x1f5e9) + "s")); //0x1f5e9 is the speech bubble
                    }
                    if (post.getInstructorAnswer() != null) {
                        getChildren().add(new ColorText(Character.toString((char) 0x1f5e9) + "i"));
                    }
                }
                if (post.getStatusLabels().contains(PostStatus.INSTRUCTOR)) {
                    getChildren().add(new ColorText("A"));
                }
                if (post.getStatusLabels().contains(PostStatus.GROUP)) {
                    getChildren().add(new ColorText("G"));
                }
                if (post.getStatusLabels().contains(PostStatus.PRIVATE)) {
                    getChildren().add(new ColorText("P"));
                }
                setSpacing(Size.width(5));
                setPadding(Size.insets(5, 0, 0, 0));
            }

            class ColorText extends Text {
                ColorText(String s) {
                    super(s);
                    setFill(Colors.textFill(ClassView.this.backgroundColor));
                    setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, Font.getDefault().getSize()));
                }
            }

            });
            getChildren().add(main);
            displayPostTextOnSidebar = true;
            if (displayPostTextOnSidebar) {
                Region gap = new Region();
                gap.setPrefHeight(Size.height(5));
                getChildren().add(gap);
            }
            Line line = new Line();
            line.setStroke(Color.DARKGRAY);
            line.setStartX(Size.width(5));
            line.setEndX(Size.width(345));
            line.setStrokeWidth(Size.width(2));
            line.setStrokeLineCap(StrokeLineCap.ROUND);
            getChildren().add(line);
            addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getButton().equals(MouseButton.SECONDARY))
                    if (expanded)
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
            if (expanded)
                return;
            Text author = new Text(post.getIdentifier().getAuthorName());
            author.setFill(Colors.textFill(backgroundColor));
            author.setFont(Font.font(Font.getDefault().getFamily(), FontPosture.ITALIC, Size.fontSize(12)));
            Text time = new Text(UtilAndConstants.parseTimestamp(new Timestamp(post.getIdentifier().getTime1())));
            time.setFill(Colors.textFill(backgroundColor));
            time.setFont(Font.font(Font.getDefault().getFamily(), FontPosture.ITALIC, Size.fontSize(12)));
            getChildren().add(2, new HBox(author, new Layouts.Filler(), time));
            Region gap = new Region();
            gap.setPrefHeight(Size.height(5));
            getChildren().add(3, gap);
            Text postCondensed = new Text(post.collapseText(230));
            postCondensed.setFont(Font.font(Size.fontSize(10)));
            getChildren().add(4, new TextFlow(postCondensed));
            Region gap2 = new Region();
            gap2.setPrefHeight(Size.height(5));
            getChildren().add(5, gap2);
            expanded = true;
        }

        void collapse() {
            if (!expanded)
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
    }

    @Override
    protected boolean isDuplicate(TaskView view) {
        return super.isDuplicate(view) && ((ClassView) view).classPd.equals(classPd);
    }
}