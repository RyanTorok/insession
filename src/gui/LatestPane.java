package gui;

import classes.Record;
import classes.RecordEntry;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import main.UtilAndConstants;

import java.util.*;
import java.util.stream.Collectors;

public class LatestPane extends VBox {


    HBox ifEmpty;
    Color refreshColor = Color.WHITE;

    public LatestPane(String title, List<Record> records, String textIfEmpty) {
        List<RecordEntry> latestEntryofEach = records.stream().map(record -> record.latest()).collect(Collectors.toList());
        Collections.sort(latestEntryofEach);
        setStyle("-fx-background-color: black; -fx-opacity: 0.8");
        setSpacing(0);
        int annoucementsToDisplay = 10;

        Text header = new Text(title);
        header.setFont(Font.font("Sans Serif",25));
        header.setFill(Color.WHITE);
        getChildren().add(new HBox(header) {
            {
                getChildren().add(new UtilAndConstants.Filler());
                setPadding(new Insets(20));
                getChildren().add(new Text(Character.toString((char) 8635)) {
                    {
                        setFill(Color.WHITE);
                        setFont(Font.font(25));
                        setOnMouseClicked(event -> {
                            System.out.println("refresh " + title); //TODO
                            RotateTransition spin = new RotateTransition(Duration.millis(500), this);
                            spin.setByAngle(360);
                            spin.setCycleCount(Animation.INDEFINITE);
                            spin.play();
                            boolean success = false; //refresh();, use another thread for net connections
                            //TODO code to wait for net thread but keep animation running
                            spin.stop();
                            spin.setToAngle(0);
                            spin.setCycleCount(1);
                            spin.setDuration(Duration.millis(1));
                            spin.play();
                            spin.setOnFinished(event1 -> {
                                if (!success) {
                                    setFill(Color.RED);
                                    refreshColor = Color.RED;

                                    Timeline colorChangeTimer = new Timeline(new KeyFrame(Duration.seconds(1), event2 -> {
                                        refreshColor = Color.WHITE;
                                        setFill(Color.WHITE);
                                    }));
                                    colorChangeTimer.play();
                                }
                            });
                        });
                        setOnMouseEntered(event -> setFill(refreshColor.equals(Color.WHITE) ? UtilAndConstants.highlightColor((Color) getFill()) : (Color) getFill()));
                        setOnMouseExited(event -> setFill(refreshColor));
                    }
                });
            }
        });
        boolean atLeastOne = false;
        for (int i = 0; i < Math.min(annoucementsToDisplay, latestEntryofEach.size()); i++) {
            atLeastOne = true;
            getChildren().add(new UpdateWrapper(latestEntryofEach.get(i), LatestPane.this));
        }
        Text placeholder = new Text(textIfEmpty) {
            {
                setFill(Color.WHITE);
                setFont(Font.font("Comfortaa", 15));
            }
        };
        ifEmpty = new HBox(placeholder) {
            {
                setHgrow(placeholder, Priority.ALWAYS);
                setAlignment(Pos.TOP_CENTER);
            }
        };
        if (!atLeastOne)
            getChildren().add(ifEmpty);

        setMinWidth(350);
        setMinHeight(850);
        setMaxHeight(850);

    }

    public void remove(UpdateWrapper updateWrapper) {
        getChildren().remove(updateWrapper);
        if (getChildren().size() <= 1) {
            //just the title
            getChildren().add(ifEmpty);
        }
    }
}
