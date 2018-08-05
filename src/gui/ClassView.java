package gui;

import classes.ClassItem;
import classes.ClassPd;
import classes.Post;
import classes.PostStatus;
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
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import main.Size;
import main.UtilAndConstants;
import net.PostEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static classes.TimeStatus.*;

public class ClassView extends TaskView {

    private ClassPd classPd;
    private HBox sideBarAndBody;
    private VBox[] sideBars;
    private GridPane[] bodyPanes;
    private Color textFill;
    private PostEngine postEngine;
    private VBox postsList;
    private boolean displayPostTextOnSidebar = false;
    private StackPane postFiltersAndList;

    public ClassView(ClassPd classPd) {
        super(classPd.getCastOf().getName() + " - P" + classPd.getPeriodNo() + " - " + classPd.getTeacherLast());
        this.classPd = classPd;
        Color color = classPd.getColor();
        textFill = UtilAndConstants.textFill(color == null ? Color.WHITE : color);
    }

    public Pane getFullDisplay() {
        return null;
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
        sideBarAndBody = new HBox(sideBars[0], bodyPanes[0]);
        VBox root = new VBox(titleBar, sideBarAndBody);

        return root;
    }

    private VBox makeTitleBar() {
        HBox titleAndControls = new HBox();

        Tab posts = new Tab(0, "posts"),
                files = new Tab(2, "files"),
                grades = new Tab(1, "assignments and grades");

        HBox tabs = new HBox(posts, files, grades);
        return new VBox(titleAndControls, tabs);
    }

    private VBox[] makeSideBars() {
        VBox[] sidebars = {makePostsSB(), makeFilesSB(), makeGradesSB()};
        for (VBox sidebar : sidebars) {
            sidebar.setPrefWidth(Size.width(400));
        }
        return sidebars;
    }

    private VBox makePostsSB() {
        postEngine = new PostEngine(classPd);
        List<Post> posts = postEngine.getPosts();

        VBox filters = makeFilters();
        postsList = new VBox();
        postFiltersAndList = new StackPane(filters, postsList);
        return new VBox(postFiltersAndList);
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
        }};
    }

    private Pane makePostSBItem(Post post) {
        return new VBox() {{
            getChildren().add(new Text(post.getTitle()));
            if (displayPostTextOnSidebar)
                getChildren().add(new TextFlow(new Text(post.getText())));
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

        return new VBox();
    }

    private VBox makeGradesSB() {
        return new VBox();
    }

    private GridPane[] makeBodyPanes() {
        return new GridPane[]{makePostsPane(), makeFilesPane(), makeGradesPane()};
    }

    private GridPane makePostsPane() {
        return new GridPane();
    }

    private GridPane makeFilesPane() {
        return new GridPane();
    }

    private GridPane makeGradesPane() {
        return new GridPane();
    }

    public Color getTextFill() {
        return textFill;
    }

    private class Tab extends Pane {

        private int index;
        private Text text;

        Tab(int index, String text) {
            this.index = index;
            this.text = new Text(text);
            getChildren().add(this.text);
            UtilAndConstants.underlineOnMouseOver(this.text);
            this.text.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> switchPane(index));
        }
    }

    private void switchPane(int index) {
        ObservableList<Node> children = sideBarAndBody.getChildren();
        children.set(0, sideBars[index]);
        children.set(1, bodyPanes[index]);
    }

}