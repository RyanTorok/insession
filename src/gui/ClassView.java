package gui;

import classes.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import main.*;
import net.PostEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static classes.TimeStatus.*;

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
    protected Pane initDisplay() {
        VBox titleBar = makeTitleBar();
        sideBars = makeSideBars();
        bodyPanes = makeBodyPanes();
        sideBarAndBody = new HBox(sideBars[0], bodyPanes[0]) {{
            setHgrow(bodyPanes[0], Priority.ALWAYS);
        }};
        return new VBox(titleBar, sideBarAndBody) {{
            setVgrow(sideBarAndBody, Priority.ALWAYS);
        }};
    }

    private VBox makeTitleBar() {
        HBox titleAndControls = new HBox();

        Tab posts = new Tab(0, "posts"),
                files = new Tab(1, "files"),
                grades = new Tab(2, "assignments and grades");

        HBox tabs = new HBox(posts, files, grades) {{setSpacing(Size.width(20));}};
        return new VBox(titleAndControls, tabs) {{
            setStyle("-fx-background-color: " + Colors.colorToHex(Color.LIGHTGRAY));
        }};
    }

    private VBox[] makeSideBars() {
        VBox[] sidebars = {makePostsSB(), makeFilesSB(), makeGradesSB()};
        for (VBox sidebar : sidebars) {
            sidebar.setPrefWidth(Size.width(400));
            sidebar.setStyle("-fx-background-color: " + Colors.colorToHex(lighter));
        }
        return sidebars;
    }

    private VBox makePostsSB() {
        postEngine = classPd.getPostEngine();
        List<Post> posts = postEngine.getPosts();

        VBox filters = makeFilters();
        postsList = makePostsList();
        postFiltersAndList = new StackPane(filters, postsList);
        return new VBox(postFiltersAndList);
    }

    private VBox makePostsList() {
        Text newThread = new Text("New Thread") {{
            setFill(lighterTextFill);
            Events.underlineOnMouseOver(this);
            setFont(CustomFonts.comfortaa(16));
            addEventHandler(MouseEvent.MOUSE_CLICKED, event -> ((PostsBody) bodyPanes[0]).newPost());
        }};
        HBox controls = new HBox();
        return new VBox() {{
            setStyle("-fx-background-color: " + Colors.colorToHex(lighter));
        }};
    }

    private VBox makeFilters() {
        List<FilterBlock> filters = new ArrayList<>();

        filters.add(new FilterBlock(this, "By Status", true,
                new Filter("Unread", PostStatus.UNREAD),
                new Filter("Updated", PostStatus.UPDATED),
                new Filter("Unanswered", PostStatus.UNANSWERED),
                new Filter("Notes", PostStatus.NOTES),
                new Filter("Questions", PostStatus.QUESTIONS),
                new Filter("My Posts", PostStatus.MINE),
                new Filter("Liked", PostStatus.LIKED),
                new Filter("Teacher Posts", PostStatus.INSTRUCTOR),
                new Filter("Class-Visible Posts", PostStatus.PUBLIC),
                new Filter("Group-Visible Posts", PostStatus.GROUP),
                new Filter("Private Posts", PostStatus.PRIVATE)
        ));
        List<ClassItem> items = classPd.getAssignmentsWithPostsDesc(10);
        List<Filter> assignments = items.stream().map(item -> new Filter(item.getName(), 0, item.getId())).collect(Collectors.toList());
        filters.add(new FilterBlock(this, "By Assignment", true, assignments));
        DatePicker picker = new DatePicker();
        picker.valueProperty().addListener((observable, oldvalue, newvalue) -> filters.get(2).getFilters().forEach(filter -> filter.setComparisonTime(UtilAndConstants.date(picker.getValue()))));
        filters.add(new FilterBlock(this, "By Date", false,
                new Filter("Today", TODAY),
                new Filter("This Week", THIS_WEEK),
                new Filter("On", ON),
                new Filter("After", AFTER),
                new Filter("Before", BEFORE)) {{
            FilterBlock me = this;
            getChildren().add(picker);
            //button to reset the date filter
            getChildren().add(new Button("Reset") {{
                me.getChildren().forEach(node -> {
                    if (node instanceof RadioButton) ((RadioButton) node).setSelected(false);
                    if (node instanceof CheckBox) ((CheckBox) node).setSelected(false);
                });
                picker.setValue(null);
            }});
        }});
        return new VBox() {{
            getChildren().addAll(filters);
            getChildren().add(new Button("Submit") {{
                setOnAction(event -> {
                    //confirm filter and switch back to post list pane
                    postEngine.setDisplayedPosts(postEngine.getPosts());
                    postEngine.getDisplayedPosts().forEach(post -> {
                        //remove posts that don't match the filter
                        for (FilterBlock block :
                                filters) {
                            if (!block.matches(post)) {
                                postEngine.getDisplayedPosts().remove(post);
                                break;
                            }
                        }
                    });
                    postsList.getChildren().clear();
                    postsList.getChildren().addAll(postEngine.getDisplayedPosts().stream().map(ClassView.this::makePostSBItem).collect(Collectors.toList()));
                    swapPostFilters();
                });
            }});
            setSpacing(20);
        }};
    }

    private Pane makePostSBItem(Post post) {
        return new VBox() {{
            getChildren().add(new Text(post.getTitle()) {{setFont(Font.font("Sans Serif", FontWeight.BOLD, Font.getDefault().getSize()));}});
            if (displayPostTextOnSidebar)
                getChildren().add(new TextFlow(new Text(post.getText())));
            addEventHandler(MouseEvent.MOUSE_CLICKED, event -> ((PostsBody) bodyPanes[0]).fire(post));

        }};
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
        Text lArrow = new Text(Character.toString((char) 0x276e)) {{
            addEventHandler(MouseEvent.MOUSE_CLICKED, event -> shiftGradesSB(gradesSB, --markingPd[0]));
        }};
        Text mpDisplay = new Text("  Grading Period " + markingPd[0] + "  ");
        Text rArrow = new Text(Character.toString((char) 0x276f)) {{
            addEventHandler(MouseEvent.MOUSE_CLICKED, event -> shiftGradesSB(gradesSB, ++markingPd[0]));
            setVisible(false);
        }};
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
        return new PostsBody(postEngine);
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

    private class Tab extends HBox {

        private int index;
        private Text text;

        Tab(int index, String text) {
            this.index = index;
            this.text = new Text(text) {{
                setFont(Font.font(Size.fontSize(16)));
            }};
            getChildren().add(this.text);
            Events.underlineOnMouseOver(this.text);
            this.text.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> switchPane(index));

        }
    }

    private void switchPane(int index) {
        ObservableList<Node> children = sideBarAndBody.getChildren();
        children.set(0, sideBars[index]);
        children.set(1, bodyPanes[index]);
    }

}