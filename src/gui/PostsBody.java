package gui;

import classes.Post;
import classes.PostStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import main.*;
import net.PostEngine;
import net.ServerSession;

import java.io.IOException;
import java.util.*;
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
        VBox toReturn = new VBox();
        AutoColoredLabel header = new AutoColoredLabel("Popular Posts", toReturn);
        header.setFont(Font.font(Size.fontSize(20)));
        HBox headerWrapper = new HBox(header);
        headerWrapper.setPadding(Size.insets(30, 0, 30, 15));

        toReturn.getChildren().add(headerWrapper);
        toReturn.getChildren().addAll(makePostDisplay(mostPopular));
        if (mostPopular.size() == 0) {
            AutoColoredLabel placeholder = new AutoColoredLabel("This class has no posts.", toReturn);
            placeholder.setFont(Font.font(Size.fontSize(14)));
            VBox placeholderWrapper = new VBox(placeholder);
            placeholderWrapper.setMinSize(Size.width(600), Size.height(100));
            placeholderWrapper.setAlignment(Pos.CENTER);
            toReturn.getChildren().add(placeholderWrapper);
        }
        toReturn.setPadding(Size.insets(10, 0, 0, 0));
        Styles.setBackgroundColor(toReturn, wrapper.getClassPd().getColor());
        toReturn.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(0), BorderWidths.DEFAULT)));
        Colors.setCustomInvertColor(toReturn, Colors.invertColor(ClassView.DEFAULT_BACKGROUND));
        Colors.autoThemeSet(toReturn, wrapper);
        return toReturn;
    }

    private VBox makeTopUnanswered(PostEngine postEngine) {
        List<Post> topUnanswered = postEngine.getPosts().stream()
                .filter(post -> post.getStatusLabels().contains(PostStatus.UNANSWERED))
                .sorted(Comparator.comparing(post -> weight_views * post.getViews() + weight_likes * post.getLikes() + weight_date * (-1 * (System.currentTimeMillis() - post.getIdentifier().getTime1()) / 86400000)))
                .collect(Collectors.toList());
        VBox toReturn = new VBox();
        AutoColoredLabel header = new AutoColoredLabel("Top Unanswered Questions", toReturn);
        header.setFont(Font.font(Size.fontSize(20)));
        HBox headerWrapper = new HBox(header);
        headerWrapper.setPadding(Size.insets(30, 0, 30, 15));
        toReturn.getChildren().add(headerWrapper);
        toReturn.getChildren().addAll(makePostDisplay(topUnanswered));
        if (topUnanswered.size() == 0) {
            AutoColoredLabel placeholder = new AutoColoredLabel("This class has no unanswered questions.", toReturn);
            placeholder.setFont(Font.font(Size.fontSize(14)));
            VBox placeholderWrapper = new VBox(placeholder);
            placeholderWrapper.setMinSize(Size.width(600), Size.height(100));
            placeholderWrapper.setAlignment(Pos.CENTER);
            toReturn.getChildren().add(placeholderWrapper);
        }
        toReturn.setPadding(Size.insets(10, 0, 0, 0));
        Styles.setBackgroundColor(toReturn, wrapper.getClassPd().getColor());
        toReturn.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(0), BorderWidths.DEFAULT)));
        Colors.setCustomInvertColor(toReturn, Colors.invertColor(ClassView.DEFAULT_BACKGROUND));
        Colors.autoThemeSet(toReturn, wrapper);
        return toReturn;
    }

    private ArrayList<VBox> makePostDisplay(List<Post> postsDescending) {
        ArrayList<VBox> postDisplay = new ArrayList<>();
        for (int i = postsDescending.size() - 1; i >= 0 && i > postsDescending.size() - 1 - NUM_DISPLAY_ON_ROOT; i--) {
            Post p = postsDescending.get(i);
            VBox headerAndPost = new VBox();
            Styles.setBackgroundColor(headerAndPost, wrapper.getBackgroundColor());
            AutoColoredLabel author = new AutoColoredLabel(p.getIdentifier().getAuthorName(), getWrapper());
            author.setFont(Font.font("Sans Serif", FontPosture.ITALIC, Size.fontSize(18)));
            AutoColoredLabel title = makeTitle(p.getTitle());
            HBox head = new HBox(title, new Layouts.Filler(), author);
            head.setPadding(Size.insets(5));
            headerAndPost.getChildren().add(head);
            TextFlow collapsed = new TextFlow(new AutoColoredLabel(p.collapseText(370), getWrapper()));
            collapsed.setPadding(Size.insets(5));
            headerAndPost.getChildren().add(collapsed);
            Events.highlightOnMouseOver(headerAndPost);
            headerAndPost.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> PostsBody.this.fire(p));
            headerAndPost.setMinSize(Size.width(600), Size.height(100));
            headerAndPost.setMaxSize(Size.width(600), Size.height(100));
            headerAndPost.setBorder(new Border(new BorderStroke(Color.BLACK, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT,
                    BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY, BorderWidths.DEFAULT, Insets.EMPTY)));
            Colors.autoThemeSet(headerAndPost, getWrapper());
            postDisplay.add(headerAndPost);
        }
        return postDisplay;
    }

    AutoColoredLabel makeTitle(String title) {
        AutoColoredLabel l = new AutoColoredLabel(title, getWrapper());
        l.setFont(Font.font("Sans Serif", Size.fontSize(18)));
        return l;
    }

    void newThread() {

        getChildren().clear();
        SubtleTextField titleField = new SubtleTextField();
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
        titleField.setBindBackground(getWrapper());
        Colors.autoThemeSet(titleField, getWrapper());
        Post newPost = Post.newPost(wrapper.getClassPd());
        PostWindow window = fire(newPost);
        window.getChildren().set(0, titleField);
        HBox postOptions = new HBox();
        InlineTextEditor.edit(window.getPostArea(), window.getPostArea().getText(), (compressedRichText)-> {
            if (compressedRichText == null) {
                initialize();
                return;
            }
            newPost.setTitle(titleField.getText());
            newPost.setFormattedText(compressedRichText);
            newPost.getIdentifier().setTime1(System.currentTimeMillis());
            newPost.getStatusLabels().add(PostStatus.MINE);
            newPost.getStatusLabels().add(PostStatus.PUBLIC);
            newPost.getStatusLabels().add(PostStatus.UNANSWERED);
            newPost.setType(Post.Type.Question);
            newPost.getIdentifier().setBelongsTo(wrapper.getClassPd());
            newPost.getIdentifier().setName(newPost.getTitle());
            newPost.setParentId(new UUID(0, 0));
            wrapper.getPostsList().getChildren().add(wrapper.new PostSBItem(newPost));
            Root.getPortal().getSearchBox().getEngine().getIndex().index(Collections.singletonList(newPost));
            getChildren().set(getChildren().indexOf(window), new PostWindow(this, newPost));
            try (ServerSession session = new ServerSession()) {
                if (session.open()) {
                    session.setPromptOnAuthenticationFailure(true);
                    //TODO add support for class item ids
                    String[] result = session.callAndResponse("newpost", wrapper.getClassPd().getUniqueId().toString(), "0", newPost.getTitle(),
                            newPost.getFormattedText().getUnformattedText(), newPost.getFormattedText().getStyleRegex(),
                            Long.toString(newPost.getVisibleTo()), Long.toString(newPost.getPosterNameVisible()),
                            newPost.getParentId().toString(), newPost.getType().toString(), Boolean.toString(newPost.isPinned()));
                    if (ServerSession.isError(result)) {
                        System.out.println(session.getErrorMsg());
                        return; //TODO notify user an error occured.
                    }
                    newPost.getIdentifier().setId(UUID.fromString(result[0]));
                    postEngine.addPost(newPost);
                } else {
                    System.out.println(session.getErrorMsg());
                }
                //check for server error
            } catch (IOException e) {

                e.printStackTrace();
            }
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

    public ClassView getWrapper() {
        return wrapper;
    }
}
