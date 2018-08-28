package gui;

import classes.RecordEntry;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import main.*;

public class UpdateWrapper extends GridPane {
    public UpdateWrapper(RecordEntry record, LatestPane holder) {
        Text message = new Text(record.toMessage());
        message.setFill(Color.WHITE);
        message.setFont(Font.font("Sans Serif", Size.fontSize(15)));
        Text classTag = new Text(record.getBelongsTo().getCastOf().getName());
        classTag.setFill(Color.WHITE);
        classTag.setFont(Font.font("Sans Serif", Size.fontSize(15)));
        Text timing = new Text(UtilAndConstants.parseTimestamp(record.getTimestamp()));
        timing.setFont(Font.font(12));
        timing.setFill(Color.WHITE);
        Text closeX = new Text(Character.toString((char) 0x00D7));
        closeX.setFont(Font.font("Sans Serif", Size.fontSize(20)));
        Color fill = Color.WHITE;
        closeX.setFill(fill);
        closeX.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> closeX.setFill(Colors.highlightColor((Color) closeX.getFill())));
        closeX.addEventHandler(MouseEvent.MOUSE_EXITED, event -> closeX.setFill(fill));
        closeX.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            Timeline closeAnimation = new Timeline(new KeyFrame(Duration.millis(200), event1 -> UpdateWrapper.this.setMaxHeight(0)));
            closeAnimation.setCycleCount(1);
            closeAnimation.setOnFinished(event1 -> {
                holder.remove(UpdateWrapper.this);
                User.active().getUpdates().remove(record.getChain());
                //need to remove from server, too (TODO)
            });
            closeAnimation.play();
        });
        this.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getTarget() != closeX)
                record.fireEvent();
        });
        this.setStyle("-fx-background-color: #000000;");
        Events.highlightOnMouseOver(this);
        add(message, 0, 0);
        add(classTag, 0, 1);
        Region filler = new Region();
        GridPane.setHgrow(filler, Priority.ALWAYS);
        add(filler, 1, 0, 1, 2);
        add(timing, 2, 0, 1, 2);
        add(closeX, 3, 0, 1, 2);
        setHgap(10);
        setPadding(Size.insets(10));

    }
}
