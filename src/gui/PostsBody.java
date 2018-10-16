package gui;

import classes.Post;
import classes.PostStatus;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import main.Events;
import main.Layouts;
import main.Root;
import main.Size;
import net.PostEngine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PostsBody extends VBox {

    private final ClassView wrapper;
    private final PostEngine postEngine;
    private int weight_views = 1;
    private int weight_likes = 2;
    private int weight_date = 3;

    private static final int NUM_DISPLAY_ON_ROOT = 3;

    public PostsBody(ClassView wrapper, PostEngine postEngine) {
        this.wrapper = wrapper;
        this.postEngine = postEngine;
        initialize();
    }

    public void initialize() {
        getChildren().clear();
        getChildren().add(new HBox(new Layouts.Filler(), makePopularPosts(postEngine), new Layouts.Filler()) {{
            setPadding(Size.insets(20, 40, 0, 40));
        }});
        getChildren().add(new Layouts.Filler());
        getChildren().add(new HBox(new Layouts.Filler(), makeTopUnanswered(postEngine), new Layouts.Filler()) {{
            setPadding(Size.insets(40));
        }});
        getChildren().add(new Layouts.Filler());
    }

    private VBox makePopularPosts(PostEngine postEngine) {
        List<Post> mostPopular = postEngine.getPosts().stream()
                .sorted(Comparator.comparing(post -> weight_views * post.getViews() + weight_likes * post.getLikes() + weight_date * (-1 * (System.currentTimeMillis() - post.getIdentifier().getTime1()) / 86400000)))
                .collect(Collectors.toList());
        Text header = new Text("Popular Posts");
        header.setFill(postEngine.getBelongsTo().textFill());
        header.setFont(Font.font(Size.fontSize(20)));
        HBox headerWrapper = new HBox(header);
        headerWrapper.setPadding(Size.insets(30, 0, 30, 15));

        VBox toReturn = new VBox(headerWrapper);
        toReturn.getChildren().addAll(makePostDisplay(mostPopular));
        if (mostPopular.size() == 0) {
            Text placeholder = new Text("This class has no posts.");
            placeholder.setFont(Font.font(Size.fontSize(14)));
            placeholder.setFill(postEngine.getBelongsTo().textFill());
            VBox placeholderWrapper = new VBox(placeholder);
            placeholderWrapper.setMinSize(Size.width(600), Size.height(100));
            placeholderWrapper.setAlignment(Pos.CENTER);
            toReturn.getChildren().add(placeholderWrapper);
        }
        toReturn.setPadding(Size.insets(10, 0, 0, 0));
        Styles.setBackgroundColor(toReturn, postEngine.getBelongsTo().getColor());
        return toReturn;
    }

    private VBox makeTopUnanswered(PostEngine postEngine) {
        List<Post> topUnanswered = postEngine.getPosts().stream()
                .filter(post -> post.getStatusLabels().contains(PostStatus.UNANSWERED))
                .sorted(Comparator.comparing(post -> weight_views * post.getViews() + weight_likes * post.getLikes() + weight_date * (-1 * (System.currentTimeMillis() - post.getIdentifier().getTime1()) / 86400000)))
                .collect(Collectors.toList());
        Text header = new Text("Top Unanswered Questions");
        header.setFill(postEngine.getBelongsTo().textFill());
        header.setFont(Font.font(Size.fontSize(20)));
        HBox headerWrapper = new HBox(header);
        headerWrapper.setPadding(Size.insets(30, 0, 30, 15));

        VBox toReturn = new VBox(headerWrapper);
        toReturn.getChildren().addAll(makePostDisplay(topUnanswered));
        if (topUnanswered.size() == 0) {
            Text placeholder = new Text("This class has no unanswered questions.");
            placeholder.setFont(Font.font(Size.fontSize(14)));
            placeholder.setFill(postEngine.getBelongsTo().textFill());
            VBox placeholderWrapper = new VBox(placeholder);
            placeholderWrapper.setMinSize(Size.width(600), Size.height(100));
            placeholderWrapper.setAlignment(Pos.CENTER);
            toReturn.getChildren().add(placeholderWrapper);
        }
        toReturn.setPadding(Size.insets(10, 0, 0, 0));
        Styles.setBackgroundColor(toReturn, postEngine.getBelongsTo().getColor());
        return toReturn;

    }

    private ArrayList<VBox> makePostDisplay(List<Post> postsDescending) {
        ArrayList<VBox> postDisplay = new ArrayList<>();
        for (int i = postsDescending.size() - 1; i >= 0 && i > postsDescending.size() - 1 - NUM_DISPLAY_ON_ROOT; i--) {
            Post p = postsDescending.get(i);
            VBox headerAndPost = new VBox();
            Styles.setBackgroundColor(headerAndPost, wrapper.getBackgroundColor());
            Text author = new Text(p.getIdentifier().getAuthorName());
            author.setFont(Font.font("Sans Serif", FontPosture.ITALIC, Size.fontSize(18)));
            Text title = makeTitle(p.getTitle());
            HBox head = new HBox(title, new Layouts.Filler(), author);
            head.setPadding(Size.insets(5));
            headerAndPost.getChildren().add(head);
            TextFlow collapsed = new TextFlow(new Text(p.collapseText(370)));
            collapsed.setPadding(Size.insets(5));
            headerAndPost.getChildren().add(collapsed);
            Events.highlightOnMouseOver(headerAndPost);
            headerAndPost.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> PostsBody.this.fire(p));
            headerAndPost.setMinSize(Size.width(600), Size.height(100));
            headerAndPost.setMaxSize(Size.width(600), Size.height(100));
            postDisplay.add(headerAndPost);
        }
        return postDisplay;
    }

    Text makeTitle(String title) {
        Text t = new Text(title);
        t.setFont(Font.font("Sans Serif", Size.fontSize(18)));
        return t;
    }

    void newThread() {

        getChildren().clear();
        TextField titleField = new TextField();
        titleField.setFont(Font.font("Sans Serif", Size.fontSize(27)));
        titleField.setPrefColumnCount(40);
        titleField.setPromptText("Enter a title for your thread here.");
        titleField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Styles.setProperty(titleField, "-fx-border-width", "0px");
                Root.getPortal().getKeyMap().lock();
            } else {
                KeyMap keyMap = Root.getPortal().getKeyMap();
                keyMap.unlock();
            }
        });
        Post newPost = Post.newPost();
        PostWindow window = fire(newPost);
        window.getChildren().set(0, titleField);
        InlineTextEditor.edit(window.getPostArea(), window.getPostArea().getText(), (compressedRichText)-> {
            if (compressedRichText == null) {
                initialize();
                return;
            }
            newPost.setTitle(titleField.getText());
            newPost.setText(compressedRichText.getUnformattedText());
            newPost.getIdentifier().setTime1(System.currentTimeMillis());
            newPost.getStatusLabels().add(PostStatus.MINE);
            newPost.getStatusLabels().add(PostStatus.PUBLIC);
            newPost.getStatusLabels().add(PostStatus.UNANSWERED);
            newPost.setType(Post.Type.Question);
            wrapper.getPostsList().getChildren().add(wrapper.new PostSBItem(newPost));
            Text title = new Text(newPost.getTitle());
            title.setFont(Font.font(Size.fontSize(24)));
            getChildren().set(getChildren().indexOf(window), new PostWindow(this, newPost));
        }, ()->{
            if (titleField.getText().length() == 0) {
                int width = (int) Size.lessWidthHeight(2);
                Styles.setProperty(titleField, "-fx-border-color", "#ff0000");
                Styles.setProperty(titleField, "-fx-border-width", width + "px");
                return false;
            }
            return true;
        });
    }

    public PostWindow fire(Post post) {
        getChildren().clear();
        PostWindow e = new PostWindow(this, post);
        getChildren().add(e);
        return e;
    }

    public PostEngine getPostEngine() {
        return postEngine;
    }

    public void deletePost(Post post) {
        postEngine.deletePost(post);
        initialize();
        wrapper.getPostsList().getChildren().removeIf(node -> {
            if (node instanceof ClassView.PostSBItem) {
                return post.equals(((ClassView.PostSBItem) node).getPost());
            }
            return false;
        });
    }
}
