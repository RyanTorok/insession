package gui;

import classes.Post;
import classes.PostStatus;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import main.*;
import net.PostEngine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PostsBody extends VBox {

    private int weight_views = 1;
    private int weight_likes = 2;
    private int weight_date = 3;

    private static final int NUM_DISPLAY_ON_ROOT = 4;

    public PostsBody(PostEngine postEngine) {
        setStyle("-fx-background-color: " + Colors.colorToHex(Color.LIGHTGRAY));
        getChildren().add(new HBox(new Layouts.Filler(), makeTopUnanswered(postEngine), new Layouts.Filler()) {{
            setPadding(Size.insets(0, 100));
        }});
        getChildren().add(new HBox(new Layouts.Filler(), makePopularPosts(postEngine), new Layouts.Filler()) {{
            setPadding(Size.insets(0, 0, 100, 0));
        }});
    }

    private VBox makePopularPosts(PostEngine postEngine) {
        List<Post> mostPopular = postEngine.getPosts().stream()
                .sorted(Comparator.comparing(post -> weight_views * post.getViews() + weight_likes * post.getLikes() + weight_date * (-1 * (System.currentTimeMillis() - post.getIdentifier().getTime1()) / 86400000)))
                .collect(Collectors.toList());
        Text header = new Text("Popular Posts") {{
            setFill(postEngine.getBelongsTo().textFill());
            setFont(Font.font(Size.fontSize(20)));
        }};
        return new VBox(header) {{
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
            setPadding(Size.insets(10));
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
        return new VBox(header) {{
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
            setPadding(Size.insets(10));
            setStyle("-fx-background-color: " + Colors.colorToHex(postEngine.getBelongsTo().getColor()));
        }};

    }

    private ArrayList<VBox> makePostDisplay(List<Post> postsDescending) {
        ArrayList<VBox> postDisplay = new ArrayList<>();
        for (int i = postsDescending.size() - 1; i >= 0 && i > postsDescending.size() - 1 - NUM_DISPLAY_ON_ROOT; i--) {
            Post p = postsDescending.get(i);
            postDisplay.add(new VBox() {{
                getChildren().add(new HBox(new Text(p.getTitle()) {{
                    setFont(Font.font("Sans Serif", FontWeight.BOLD, Size.fontSize(18)));
                }}, new Text(p.getIdentifier().getAuthorName()) {{
                    setFont(Font.font("Sans Serif", FontPosture.ITALIC, Size.fontSize(18)));
                }}));
                getChildren().add(new TextFlow(new Text(p.getText())));
                Events.highlightOnMouseOver(this);
                addEventHandler(MouseEvent.MOUSE_CLICKED, event -> PostsBody.this.fire(p));
            }});
        }
        return postDisplay;
    }

    void newPost() {

    }

    public void fire(Post post) {

    }
}
