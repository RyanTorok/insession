package gui;

import classes.Post;
import classes.PostStatus;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.*;
import main.*;
import net.PostEngine;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PostsBody extends VBox {

    private int weight_views = 1;
    private int weight_likes = 2;
    private int weight_date = 3;

    private static final int NUM_DISPLAY_ON_ROOT = 3;

    public PostsBody(PostEngine postEngine) {
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
        Text header = new Text("Popular Posts") {{
            setFill(postEngine.getBelongsTo().textFill());
            setFont(Font.font(Size.fontSize(20)));
        }};
        HBox headerWrapper = new HBox(header) {{
            setPadding(Size.insets(30, 0, 30, 15));
        }};
        return new VBox(headerWrapper) {{
            getChildren().addAll(makePostDisplay(mostPopular));
            if (mostPopular.size() == 0) {
                getChildren().add(new VBox(new Text("This class has no posts.") {{
                    setFont(Font.font(Size.fontSize(14)));
                    setFill(postEngine.getBelongsTo().textFill());
                }}) {{
                    setMinSize(Size.width(600), Size.height(100));
                    setAlignment(Pos.CENTER);
                }});
            }
            setStyle("-fx-background-color: " + Colors.colorToHex(postEngine.getBelongsTo().getColor()));
            setPadding(Size.insets(10, 0, 0, 0));
        }};
    }

    private VBox makeTopUnanswered(PostEngine postEngine) {
        List<Post> topUnanswered = postEngine.getPosts().stream()
                .filter(post -> post.getStatusLabels().contains(PostStatus.UNANSWERED))
                .sorted(Comparator.comparing(post -> weight_views * post.getViews() + weight_likes * post.getLikes() + weight_date * (-1 * (System.currentTimeMillis() - post.getIdentifier().getTime1()) / 86400000)))
                .collect(Collectors.toList());
        Text header = new Text("Top Unanswered Questions") {{
            setFill(postEngine.getBelongsTo().textFill());
            setFont(Font.font(Size.fontSize(20)));
        }};
        HBox headerWrapper = new HBox(header) {{
            setPadding(Size.insets(30, 0, 30, 15));
        }};
        String testText = "This is a test post; does it show 0up? ";
        for (int i = 0; i < 10; i++) {
            testText = testText + testText;
        }
        return new VBox(headerWrapper) {{
            getChildren().addAll(makePostDisplay(topUnanswered));
            if (topUnanswered.size() == 0) {
                getChildren().add(new VBox(new Text("This class has no unanswered questions.") {{
                    setFont(Font.font(Size.fontSize(14)));
                    setFill(postEngine.getBelongsTo().textFill());
                }}) {{
                    setMinSize(Size.width(600), Size.height(100));
                    setAlignment(Pos.CENTER);
                }});
            }
            setPadding(Size.insets(10, 0, 0, 0));
            setStyle("-fx-background-color: " + Colors.colorToHex(postEngine.getBelongsTo().getColor()));
        }};

    }

    private ArrayList<VBox> makePostDisplay(List<Post> postsDescending) {
        ArrayList<VBox> postDisplay = new ArrayList<>();
        for (int i = postsDescending.size() - 1; i >= 0 && i > postsDescending.size() - 1 - NUM_DISPLAY_ON_ROOT; i--) {
            Post p = postsDescending.get(i);
            postDisplay.add(new VBox() {{
                setStyle("-fx-background-color: " + Colors.colorToHex(Color.LIGHTGRAY));
                getChildren().add(new HBox(new Text(p.getTitle()) {{
                    setFont(Font.font("Sans Serif", Size.fontSize(18)));
                }}, new Layouts.Filler(), new Text(p.getIdentifier().getAuthorName()) {{
                    setFont(Font.font("Sans Serif", FontPosture.ITALIC, Size.fontSize(18)));
                }}) {{
                    setPadding(Size.insets(5));
                }});
                getChildren().add(new TextFlow(new Text(p.collapseText(370))) {{
                    setPadding(Size.insets(5));
                }});
                Events.highlightOnMouseOver(this);
                addEventHandler(MouseEvent.MOUSE_CLICKED, event -> PostsBody.this.fire(p));
                setMinSize(Size.width(600), Size.height(100));
                setMaxSize(Size.width(600), Size.height(100));
            }});
        }
        return postDisplay;
    }

    void newPost() {

    }

    public void fire(Post post) {
        getChildren().clear();
        getChildren().add(new PostWindow(post));
    }

    private class PostWindow extends VBox {

        HBox titleBar;
        TextFlow text;

        PostWindow(Post post) {
            titleBar = new HBox();

            Text title = new Text(post.getTitle());
            title.setFont(Font.font(Size.fontSize(24)));
            titleBar.getChildren().add(title);
            titleBar.getChildren().add(new Layouts.Filler());
            boolean updated = post.getIdentifier().getTime1() < post.getIdentifier().getTime2();

            Text uploadedText = new Text((updated ? "Updated " : "Uploaded ") + UtilAndConstants.parseTimestamp(new Timestamp(updated ? post.getIdentifier().getTime2() : post.getIdentifier().getTime1())) + " by ");
            uploadedText.setFont(Font.font(Size.fontSize(14)));

            Text name = new Text(post.getIdentifier().getAuthorName());
            name.setFont(Font.font(Size.fontSize(14)));
            Events.underlineOnMouseOver(name);
            name.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> Root.getPortal().launchTaskView(new UserProfile(User.fromId(post.getPosterId()))));

            titleBar.getChildren().addAll(uploadedText, name);

            Region gap = new Region();
            gap.setPrefWidth(Size.width(10));
            titleBar.getChildren().add(gap);

            Shape userImage = new ShapeImage(new Circle(Size.lessWidthHeight(20)), post.isPosterNameVisible() || post.getPosterId() == User.active().getUniqueID() ? Images.getUserPicture(post.getPosterId()) : Images.defaultUserImage()).apply();
            userImage.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> Root.getPortal().launchTaskView(new UserProfile(User.fromId(post.getPosterId()))));
            titleBar.getChildren().add(userImage);

            titleBar.setAlignment(Pos.CENTER_LEFT);
            text = post.getFormattedText().asTextFlow();
            getChildren().addAll(titleBar, text);
            setPadding(Size.insets(20));
        }

    }
}
