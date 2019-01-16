package gui;

import classes.ClassItem;
import classes.Post;
import classes.PostStatus;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import main.*;

import java.sql.Timestamp;

class PostWindow extends VBox {

    private final PostArea postArea;
    HBox titleBar;
    TextFlow text;
    private PostsBody wrapper;

    PostWindow(PostsBody wrapper, Post post) {
        this.wrapper = wrapper;
        titleBar = new HBox();
        Text title = new Text(post.getTitle());
        title.setFont(Font.font(Size.fontSize(24)));
        titleBar.getChildren().add(title);
        Region region = new Region();
        region.setPrefWidth(Size.width(20));
        titleBar.getChildren().add(region);
        ClassItem classItem = ClassItem.fromId(post.getClassItemId());
        String CIname = classItem == null ? "null" : classItem.getName();
        Text typeText = new Text((post.getType().equals(Post.Type.Question) ? "Question" : "Note") + " about " + CIname);
        typeText.setFont(Font.font("Sans Serif", FontPosture.ITALIC, Size.fontSize(14)));
        titleBar.getChildren().add(typeText);
        titleBar.getChildren().add(new Layouts.Filler());
        boolean updated = post.getStatusLabels().contains(PostStatus.UPDATED);

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

        Shape userImage = new ShapeImage(new Circle(Size.lessWidthHeight(20)), post.getPosterNameVisible() == 0 || post.getPosterId() == User.active().getUniqueID() ? Images.getUserPicture(post.getPosterId()) : Images.defaultUserImage()).apply();
        userImage.setCursor(Cursor.CLOSED_HAND);
        userImage.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> Root.getPortal().launchTaskView(new UserProfile(User.fromId(post.getPosterId()))));
        titleBar.getChildren().add(userImage);

        titleBar.setAlignment(Pos.CENTER_LEFT);
        text = post.getFormattedText().extract();
        post.getStatusLabels().add(PostStatus.MINE);
        postArea = new PostArea(this, post, text, post.getStatusLabels().contains(PostStatus.MINE) || post.getType() == Post.Type.Student_Answer);
        getChildren().addAll(titleBar, postArea);
        setPadding(Size.insets(30));
        setSpacing(Size.height(20));
    }

    public PostsBody getWrapper() {
        return wrapper;
    }

    public void setWrapper(PostsBody wrapper) {
        this.wrapper = wrapper;
    }

    public PostArea getPostArea() {
        return postArea;
    }
}
