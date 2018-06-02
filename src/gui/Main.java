package gui;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.Root;
import main.Student;
import main.User;

import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main extends Application {

    BarMenu menus[] = new BarMenu[6];
    int currentMenu = 0;
    Text subtitle;
    boolean caps = Toolkit.getDefaultToolkit().getLockingKeyState(java.awt.event.KeyEvent.VK_CAPS_LOCK);


    public static final int BASE_STATE = 0;
    public static final int SLEEP_STATE = 1;
    public static final int TERMINAL_STATE = 2;

    Node top_bar;
    Node sleepBody;
    Node mainBody;
    Text clock;
    Text date;
    Terminal term;
    StackPane mainArea;
    int state = 0;
    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    private void newUser() {
        Scene window = NewUserWindow.get(this);
        primaryStage.setScene(window);
        primaryStage.show();
    }

    @Override
    public void start(Stage primaryStage) {
        //create taskbar icon
        this.primaryStage = primaryStage;
        Root.setPortal(this);
        User user = User.read();
        String icon_path = System.getProperty("user.dir") + File.separator + "resources" + File.separator + "icon.png" + File.separator;
        Image icon = new Image("file:" + icon_path);
        primaryStage.getIcons().add(icon);
        primaryStage.setTitle("Paintbrush LMS");
        ImageView imageView = new ImageView(icon);
        Root.setActiveUser(user);
        if (user == null || User.getSerCount() > 1) {
                newUser();
        } else {
            switchToMain();
        }
    }

    void switchToMain() {
        primaryStage.setMaximized(true);
        primaryStage.setTitle("Welcome, " + Root.getActiveUser().getFirst() + " - Paintbrush LMS");
        Text mainlogo = new Text("paintbrush.    ");
        mainlogo.setFont(Font.font("Comfortaa", 60));
        mainlogo.setFill(Color.WHITE);
        final String upper = mainlogo.getText().substring(0, mainlogo.getText().length() - 2).toUpperCase();
        final String lower = mainlogo.getText();

        Text subText = new Text("Let's get something done today.");
        subtitle = subText;
        subText.setFont(Font.font("Sans Serif", FontPosture.ITALIC, 20));
        subText.setFill(Color.WHITE);
        VBox titles = new VBox(mainlogo, subText);
        titles.setSpacing(5);

        //scroll links
        menus[0] = new BarMenu("latest", 0);
        menus[1] = new BarMenu("classes", 1);
        menus[2] = new BarMenu("organizations", 2);
        menus[3] = new BarMenu("browse lessons", 3);
        menus[4] = new BarMenu("community", 4);
        menus[5] = new BarMenu(Root.getActiveUser() == null ? "not signed in" : "signed in as " + Root.getActiveUser().getFirst() + " " + Root.getActiveUser().getLast(), 5);

        menus[0].setFont(Font.font(menus[0].getFont().getFamily(), FontWeight.BOLD, menus[0].getFont().getSize()));

        HBox topbar = new HBox(titles, menus[0], menus[1], menus[2], menus[3], menus[4], menus[5]);
        top_bar = topbar;
        topbar.setSpacing(35);
        topbar.setAlignment(Pos.CENTER_LEFT);
        topbar.setStyle("-fx-background-color: #000000");
        topbar.setPadding(new Insets(15));

        //clock
        final Text clock = new Text("");
        this.clock = clock;
        final Text date = new Text("");
        this.date = date;
        clock.setFill(Color.WHITE);
        clock.setFont(Font.font("Sans Serif", FontWeight.NORMAL, 100));
        date.setFill(Color.WHITE);
        date.setFont(Font.font("Sans Serif", FontWeight.NORMAL, 45));
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Main.this.updateTime();
            }
        }, 0, 500);
        BorderPane sleepbody = new BorderPane();
        sleepBody = sleepbody;
        HBox sleep_btm = new HBox(new VBox(clock,date));
        sleep_btm.setAlignment(Pos.BOTTOM_CENTER);
        sleepbody.setBottom(sleep_btm);
        sleepbody.setVisible(false);


        BorderPane body = new BorderPane();
        mainBody = body;
        ImageView backgd = new ImageView(new Image("file:" + System.getProperty("user.dir") + File.separator + "resources" + File.separator + "background.jpeg"));
        backgd.setFitWidth(1900);
        backgd.setPreserveRatio(true);
        StackPane allBodyPanes = new StackPane(sleepbody, body);
        VBox root = new VBox(topbar, allBodyPanes);
        Terminal term = new Terminal(this);
        this.term = term;
        AnchorPane terminalpane = new AnchorPane(term);
        terminalpane.setPrefHeight(649);
        terminalpane.setPrefWidth(999);
        term.setVisible(false);

        StackPane mainArea = new StackPane(backgd, terminalpane, root);
        this.mainArea = mainArea;
        primaryStage.setScene(new Scene(mainArea, 999, 649));

        primaryStage.getScene().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (state == SLEEP_STATE) {
                if (event.getCode().equals(KeyCode.ESCAPE))
                    return;
                wakeup();
                if (event.getCode().equals(KeyCode.CAPS)) {
                    caps = !caps;
                }
                return;
            }
            if (event.getCode().equals(KeyCode.SPACE)) {

                if (state == BASE_STATE) {
                    term.setVisible(true);
                    term.start();
                    state = TERMINAL_STATE;
                    ObservableList<Node> workingCollection = FXCollections.observableArrayList(mainArea.getChildren());
                    Collections.swap(workingCollection, 1, 2);
                    mainArea.getChildren().setAll(workingCollection);
                }
            }

            if (event.getCode().equals(KeyCode.ESCAPE)) {
                if (state == BASE_STATE) {
                    sleep();
                }
                else if (state == TERMINAL_STATE) {
                    if (event.isControlDown()) {
                        term.clearTerminal();
                    }
                    quitTerminal();
                }
            }
        });

        primaryStage.getScene().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().equals(KeyCode.LEFT) && state == BASE_STATE) {
                if (currentMenu != 0)
                    scrollBody(currentMenu - 1, subtitle);
            }
            if (event.getCode().equals(KeyCode.RIGHT) && state == BASE_STATE) {
                if (currentMenu != menus.length - 1)
                    scrollBody(currentMenu + 1, subtitle);
            }
            if (event.getCode().equals(KeyCode.CAPS)) {
                caps = !caps;
                mainlogo.setText(caps ? upper : lower);
            }
        });

        primaryStage.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (state == SLEEP_STATE) {
                wakeup();
            }
        });
        primaryStage.show();
    }

    private void updateTime() {
        if (state == SLEEP_STATE) {
            String time = new SimpleDateFormat("EEEEEEEE, MMMMMMMMM dd, YYYY  h:mm:ss aa").format(new Date());
            String[] timeanddate = time.split("  ");
            clock.setText(timeanddate[1]);
            date.setText(timeanddate[0]);
        }
    }

    private void wakeup() {
        state = BASE_STATE;
        top_bar.setVisible(true);
        FadeTransition fadein = new FadeTransition(Duration.millis(200), top_bar);
        fadein.setFromValue(0);
        fadein.setToValue(1);
        fadein.play();
        FadeTransition fadein_ = new FadeTransition(Duration.millis(200), mainBody);
        fadein_.setFromValue(0);
        fadein_.setToValue(1);
        fadein_.play();
        sleepBody.setVisible(false);
    }

    private void sleep() {
        state = SLEEP_STATE;
        top_bar.setVisible(false);
        mainBody.setVisible(false);
        FadeTransition ft = new FadeTransition(Duration.millis(200), sleepBody);
        ft.setFromValue(0);
        ft.setToValue(1);
        updateTime();
        sleepBody.setVisible(true);
        ft.play();
    }

    class BarMenu extends Text {
        int scrollPos;
        public BarMenu(String text, int order) {
            super (text);
            scrollPos = order;
            addEventHandler(MouseEvent.MOUSE_ENTERED, event -> this.setUnderline(true));
            addEventFilter(MouseEvent.MOUSE_EXITED, event -> this.setUnderline(false));
            addEventHandler(MouseEvent.MOUSE_CLICKED, event -> Main.this.scrollBody(scrollPos, Main.this.subtitle));
            setFont(Font.font("Confortaa", 20));
            setFill(Color.WHITE);
            setTextAlignment(TextAlignment.CENTER);
        }
    }

    private void scrollBody(int scrollPos, Text changeText) {
        String subtitles[] = {
                "Let's get something done today.",
                "Let's learn something today.",
                "Let's do something we love today.",
                "Let's find a new interest today.",
                "Let's see what the world did today.",
                "Let's make ourselves better today."
        };
        changeText.setText(subtitles[scrollPos]);
        currentMenu = scrollPos;
        BarMenu m = menus[scrollPos];
        for (BarMenu m1 :
                menus) {
            m1.setFont(Font.font(m.getFont().getFamily(), FontWeight.NORMAL, m.getFont().getSize()));
        }
        m.setFont(Font.font(m.getFont().getFamily(), FontWeight.BOLD, m.getFont().getSize()));
    }

    void quitTerminal() {
        term.exit();
        state = BASE_STATE;
        ObservableList<Node> workingCollection = FXCollections.observableArrayList(mainArea.getChildren());
        Collections.swap(workingCollection, 1, 2);
        mainArea.getChildren().setAll(workingCollection);
    }

    @Override
    public void stop() throws Exception {
        Root.saveAll();
        System.exit(1);
    }
}


