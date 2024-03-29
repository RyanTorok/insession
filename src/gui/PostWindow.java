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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
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
        AutoColoredLabel title = new AutoColoredLabel(post.getTitle(), wrapper.getWrapper());
        title.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            //Layouts.expandText(title);
        });
        title.setFont(Font.font(Size.fontSize(24)));
        titleBar.getChildren().add(title);
        Region region = new Region();
        region.setPrefWidth(Size.width(20));
        titleBar.getChildren().add(region);
        ClassItem classItem = ClassItem.fromId(post.getClassItemId());
        String CIname = classItem == null ? "null" : classItem.getName();
        AutoColoredLabel typeText = new AutoColoredLabel( "tagged: " , wrapper.getWrapper());
        AutoColoredLabel CIlabel = new AutoColoredLabel(CIname, wrapper.getWrapper());
        Events.underlineOnMouseOver(CIlabel);
        CIlabel.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> wrapper.getWrapper().goToClassItem(classItem));
        typeText.setFont(Font.font("Sans Serif", FontPosture.ITALIC, Size.fontSize(14)));
        CIlabel.setFont(Font.font("Sans Serif", FontPosture.ITALIC, Size.fontSize(14)));
        titleBar.getChildren().add(new VBox(new Layouts.Filler(), new AutoColoredTextFlow(wrapper.getWrapper(), typeText, CIlabel), new Layouts.Filler()));
        titleBar.getChildren().add(new Layouts.Filler());
        titleBar.setSpacing(Size.width(10));
        boolean updated = post.getStatusLabels().contains(PostStatus.UPDATED);

        AutoColoredLabel uploadedText = new AutoColoredLabel((updated ? "Updated " : "Uploaded ") + UtilAndConstants.parseTimestamp(new Timestamp(updated ? post.getIdentifier().getTime2() : post.getIdentifier().getTime1())) + " by ", wrapper.getWrapper());
        uploadedText.setFont(Font.font(Size.fontSize(14)));

        AutoColoredLabel name = new AutoColoredLabel(post.getIdentifier().getAuthorName(), wrapper.getWrapper());
        name.setFont(Font.font(Size.fontSize(14)));
        Events.underlineOnMouseOver(name);
        name.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> Root.getPortal().launchTaskView(new UserProfile(User.fromId(post.getPosterId()))));

        titleBar.getChildren().addAll(new VBox(new Layouts.Filler(), new AutoColoredTextFlow(wrapper.getWrapper(), uploadedText, name), new Layouts.Filler()));

        Region gap = new Region();
        gap.setPrefWidth(Size.width(10));
        titleBar.getChildren().add(gap);

        Shape userImage = new ShapeImage(new Circle(Size.lessWidthHeight(20)), post.getPosterNameVisible() == 0 || post.getPosterId() == User.active().getUniqueID() ? Images.getUserPicture(post.getPosterId()) : Images.defaultUserImage()).apply();
        userImage.setCursor(Cursor.CLOSED_HAND);
        userImage.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> Root.getPortal().launchTaskView(new UserProfile(User.fromId(post.getPosterId()))));
        titleBar.getChildren().add(userImage);

        titleBar.setAlignment(Pos.CENTER_LEFT);
        text = post.getFormattedText().extract(wrapper.getWrapper());
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
