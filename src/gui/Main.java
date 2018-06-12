package gui;

import javafx.animation.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.Root;
import main.User;
import main.UtilAndConstants;
import terminal.Address;

import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main extends Application {

    BarMenu menus[] = new BarMenu[5];
    int currentMenu = 0;
    Text subtitle;
    boolean caps = Toolkit.getDefaultToolkit().getLockingKeyState(java.awt.event.KeyEvent.VK_CAPS_LOCK);


    public static final int BASE_STATE = 0;
    public static final int SLEEP_STATE = 1;
    public static final int TERMINAL_STATE = 2;
    public static final int SIDEBAR_STATE = 3;

    Node top_bar;
    Node sleepBody;
    Node mainBody;
    Text clock;
    Text date;
    Node picture;
    Terminal term;
    StackPane mainArea;
    SideBar sideBar;
    Text temperature;
    Text weatherDesc;
    static final DecimalFormat tempFormat = new DecimalFormat("##.#");
    WeatherManager manager;
    Integer state = 0;
    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    public void newUser() {
        primaryStage.setMaximized(false);
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
        String icon_path = Address.root_addr + File.separator + "resources" + File.separator + "icon.png";
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

    public void switchToMain() {
        primaryStage.setMaximized(false);
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
        BarMenu name = new BarMenu(Root.getActiveUser() == null || Root.getActiveUser().getUsername() == null ? "Not signed in" : Root.getActiveUser().getFirst() + " " + Root.getActiveUser().getLast(), -1);
        for (BarMenu m: menus
             ) {
                m.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    if (state == SIDEBAR_STATE)
                        UtilAndConstants.fireMouse(name, MouseEvent.MOUSE_CLICKED);
                    scrollBody(m.scrollPos, subtitle);
                });
        }
        menus[0].setFont(Font.font(menus[0].getFont().getFamily(), FontWeight.BOLD, menus[0].getFont().getSize()));

        name.setOnMouseClicked(event -> {
            if (state == BASE_STATE) {
                name.setFont(Font.font(name.getFont().getFamily(), FontWeight.BOLD, name.getFont().getSize()));
                sideBar.enter();
                state = SIDEBAR_STATE;
            }
            else if (state == SIDEBAR_STATE) {
                name.setFont(Font.font(name.getFont().getFamily(), FontWeight.NORMAL, name.getFont().getSize()));
                sideBar.disappear();
                state = BASE_STATE;
            }
        });

        Region filler = new Region();
        HBox.setHgrow(filler, Priority.ALWAYS);
        Image image = Root.getActiveUser().getAcctImage();
        Shape picture = new ShapeImage(new Circle(30), image).apply();
        this.picture = picture;
        HBox topbar = new HBox(titles, menus[0], menus[1], menus[2], menus[3], menus[4], filler, name, picture);
        top_bar = topbar;
        topbar.setSpacing(35);
        topbar.setAlignment(Pos.CENTER_LEFT);
        String color = UtilAndConstants.colorToHex(Root.getActiveUser().getAccentColor());
        String borderWidth =  ".67em";
        topbar.setStyle("-fx-background-color: #000000; -fx-border-color: " + color + "; -fx-border-width: 0em 0em " + borderWidth + " 0em; -fx-border-style: solid");
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

        //weather display
        AnchorPane weatherPane = new AnchorPane();
        manager = new WeatherManager(Root.getActiveUser().getZipcode());
        manager.update();
        temperature = new Text();
        if (Root.getActiveUser().usesFahrenheit()) {
            temperature.setText(tempFormat.format(manager.getTempFahrenheit()) + (char) 0x00B0 + "F");
        } else {
            temperature.setText(tempFormat.format(manager.getTempCelsius()) + (char) 0x00B0 + "C");
        }
        temperature.setFill(Color.WHITE);
        temperature.setFont(Font.font("Sans Serif", FontWeight.NORMAL, 100));
        weatherDesc = new Text(manager.getDescription());
        weatherDesc.setFill(Color.WHITE);
        weatherDesc.setFont(Font.font("Sans Serif", FontWeight.NORMAL, 45));
        VBox weatherDetails = new VBox(weatherDesc);
        VBox weatherDisplay = new VBox(temperature, weatherDetails);
        weatherDisplay.setAlignment(Pos.CENTER_RIGHT);
        HBox sleep_btm = new HBox(new VBox(clock,date), new UtilAndConstants.Filler(), weatherDisplay);
        sleep_btm.setAlignment(Pos.BOTTOM_LEFT);

        //synthesize sleep body
        sleepbody.setBottom(sleep_btm);
        sleepbody.setPadding(new Insets(30));
        sleepbody.setVisible(false);


        BorderPane body = new BorderPane();
        mainBody = body;
        ImageView backgd = new ImageView(new Image("file:" + Address.root_addr + File.separator + "resources" + File.separator + "background.jpeg"));
        backgd.setFitWidth(1900);
        backgd.setPreserveRatio(true);
        StackPane allBodyPanes = new StackPane(sleepbody, body);
        VBox root = new VBox(topbar, allBodyPanes);
        ScrollPane terminalWrapper = new ScrollPane();
        Terminal term = new Terminal(this, terminalWrapper);
        this.term = term;
        terminalWrapper.setContent(term);
        terminalWrapper.setFitToHeight(true);
        terminalWrapper.setStyle("-fx-background-color: #202020");
        terminalWrapper.setPrefWidth(term.getPrefWidth());
        terminalWrapper.setPrefHeight(term.getPrefHeight());
        terminalWrapper.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        terminalWrapper.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        terminalWrapper.setFitToWidth(true);
        AnchorPane terminalpane = new AnchorPane(terminalWrapper);
        terminalpane.setPrefHeight(649);
        terminalpane.setPrefWidth(999);
        terminalpane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            //not the actual terminal
            if (state == TERMINAL_STATE && event.getTarget() == terminalpane)
                quitTerminal();
            else term.current.requestFocus();
        });
        term.setVisible(false);

        switch (manager.getCurrent()) {
            case Snow: snow(weatherPane, 75); break;
            case Blizzard: snow(weatherPane, 200); break;
            default: //snow(weatherPane, 200);
                break;
        }
        StackPane mainArea = new StackPane(backgd, weatherPane, terminalpane, root);
        this.mainArea = mainArea;
        primaryStage.setScene(new Scene(mainArea, 999, 649));
        primaryStage.setMaximized(true);

        //sidebar
        sideBar = new SideBar(this);
        allBodyPanes.getChildren().add(sideBar);
        body.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getTarget() != sideBar && state == SIDEBAR_STATE) {
                UtilAndConstants.fireMouse(name, MouseEvent.MOUSE_CLICKED);
                state = BASE_STATE;
            }
        });

        picture.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            UtilAndConstants.fireMouse(name, MouseEvent.MOUSE_CLICKED);
        });

        primaryStage.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (state == SLEEP_STATE)
                wakeup();
        });


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

                if (state == BASE_STATE && event.isControlDown()) {
                    UtilAndConstants.fireMouse(name, MouseEvent.MOUSE_CLICKED);
                }

                else if (state == BASE_STATE || state == SIDEBAR_STATE && !event.isControlDown()) {
                    if (state == SIDEBAR_STATE)
                        UtilAndConstants.fireMouse(name, MouseEvent.MOUSE_CLICKED);
                    term.setVisible(true);
                    term.start();
                    state = TERMINAL_STATE;
                    ObservableList<Node> workingCollection = FXCollections.observableArrayList(mainArea.getChildren());
                    Collections.swap(workingCollection, 2, 3);
                    mainArea.getChildren().setAll(workingCollection);
                }

                else if (state == SIDEBAR_STATE) {
                    UtilAndConstants.fireMouse(name, MouseEvent.MOUSE_CLICKED);
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
                } else if (state == SIDEBAR_STATE) {
                    UtilAndConstants.fireMouse(name, MouseEvent.MOUSE_CLICKED);
                }
            }
        });

        primaryStage.getScene().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode().equals(KeyCode.LEFT) && state == BASE_STATE) {
                if (currentMenu != 0)
                    scrollBody(currentMenu - 1, subtitle);
            }
            if (event.getCode().equals(KeyCode.RIGHT) && state == BASE_STATE) {
                if (currentMenu != menus.length - 1)
                    scrollBody(currentMenu + 1, subtitle);
            }
            if (event.getCode().equals(KeyCode.CAPS)) {
                caps = Toolkit.getDefaultToolkit().getLockingKeyState(java.awt.event.KeyEvent.VK_CAPS_LOCK);
                mainlogo.setText(caps ? upper : lower);
                if (caps)
                    subtitle.setText("Live your life in ALL CAPS today.");
                else {
                    subtitle.setText(subtitles[currentMenu]);
                }
            }
        });

        state = BASE_STATE;
        primaryStage.show();
    }

    public void updateWeather() {
        manager.update();
        if (Root.getActiveUser().usesFahrenheit()) {
            temperature.setText(tempFormat.format(manager.getTempFahrenheit()) + (char) 0x00B0 + "F");
        } else {
            temperature.setText(tempFormat.format(manager.getTempCelsius()) + (char) 0x00B0 + "C");
        }
        weatherDesc.setText(manager.getDescription());
    }

    private void snow(AnchorPane weatherPane, int particlesPerSecond) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.0 / particlesPerSecond), event -> {
           weatherPane.getChildren().add(new SnowParticle(weatherPane));
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void updateTime() {
        if (state == SLEEP_STATE) {
            String time;
            if (Root.getActiveUser().isClock24Hour())
                time = new SimpleDateFormat( "EEEEEEEE, MMMMMMMMM d, YYYY  H:mm:ss").format(new Date());
            else
                time = new SimpleDateFormat( "EEEEEEEE, MMMMMMMMM d, YYYY  h:mm:ss aa").format(new Date());
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

    public void clearStage() {
        primaryStage.setScene(null);
    }

    public void resetMain() {
        clearStage();
        primaryStage.setMaximized(false);
        switchToMain();
    }

    class BarMenu extends Text {
        int scrollPos;
        public BarMenu(String text, int order) {
            super (text);
            scrollPos = order;
            addEventHandler(MouseEvent.MOUSE_ENTERED, event -> this.setUnderline(true));
            addEventFilter(MouseEvent.MOUSE_EXITED, event -> this.setUnderline(false));
            setFont(Font.font("Confortaa", 20));
            setFill(Color.WHITE);
            setTextAlignment(TextAlignment.CENTER);
        }
    }

    String subtitles[] = {
            "Let's get something done today.",
            "Let's learn something today.",
            "Let's do something we love today.",
            "Let's find a new interest today.",
            "Let's see what the world did today.",
    };

    private void scrollBody(int scrollPos, Text changeText) {
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
        Collections.swap(workingCollection, 2, 3);
        mainArea.getChildren().setAll(workingCollection);
    }

    @Override
    public void stop() {
        Root.saveAll();
        System.exit(1);
    }

    class SideBar extends VBox {
        private Main holder;

        private TranslateTransition init;
        private TranslateTransition in;
        private TranslateTransition out;
        private int selectedMenu = -1;
        private Color color;

        ArrayList<Menu> menus;

        public SideBar(Main holder) {
            super();
            this.holder = holder;
            setVisible(false);

            setPrefWidth(200);
            setPrefHeight(1000);
            setStyle("-fx-background-color: " + UtilAndConstants.colorToHex(Root.getActiveUser().getAccentColor()));
            //initial placement
            init = new TranslateTransition();
            init.setNode(this);
            init.setDuration(Duration.millis(1));
            init.setByX(1900);
            init.setAutoReverse(false);
            init.play();

            //enter screen animation
            in = new TranslateTransition();
            in.setNode(this);
            in.setDuration(Duration.millis(200));
            in.setByX(-300);
            in.setAutoReverse(false);

            //exit screen animation
            out = new TranslateTransition();
            out.setNode(this);
            out.setDuration(Duration.millis(200));
            out.setByX(300);
            out.setAutoReverse(false);

            menus = new ArrayList<>();

            boolean signedIn = Root.getActiveUser() != null && Root.getActiveUser().getUsername() != null;
            Menu openTerminal = new Menu("Open Terminal");
            Menu grades = new Menu("My Grades");
            Menu attendance = new Menu("Attendance History");
            Menu accountSettings = new Menu("Account Settings");
            Menu kbShortcuts = new Menu("Keyboard Shortcuts");
            Menu history = new Menu("Usage History");
            Menu privacy = new Menu("Privacy Policy");
            Menu help = new Menu("Help");
            Menu switch_user = new Menu(signedIn ? "Switch User" : "Sign in");
            Menu save = new Menu(signedIn ? "Save and Exit" : "Exit");

            menus.add(openTerminal);
            menus.add(grades);
            menus.add(attendance);
            menus.add(accountSettings);
            menus.add(kbShortcuts);
            menus.add(history);
            menus.add(privacy);
            menus.add(help);
            menus.add(switch_user);
            menus.add(save);
            
            openTerminal.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> primaryStage.getScene().getRoot().fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, " ", " ", KeyCode.SPACE, false, false, false, false)));

            grades.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                //TODO
            });

            attendance.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                //TODO
            });

            accountSettings.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (Root.getActiveUser() != null && Root.getActiveUser().getUsername() != null) {
                    UtilAndConstants.fireMouse(Main.this.picture, MouseEvent.MOUSE_CLICKED);
                    new AcctSettings().show();
                }
            });

            history.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                //TODO
            });

            privacy.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                //TODO
            });

            help.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                //TODO
            });

            switch_user.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                Main.this.primaryStage.setMaximized(false);
                Root.saveAll();
                Main.this.newUser();
            });

            save.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                Main.this.stop();
            });
            getChildren().addAll(menus);
            setAlignment(Pos.TOP_CENTER);
            primaryStage.getScene().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
                if (state == SIDEBAR_STATE) {
                    if (event.getCode().equals(KeyCode.UP)) {
                        if (selectedMenu == 0 || selectedMenu == -1) {
                            scroll(0, menus.size() - 1);
                            selectedMenu = menus.size() - 1;
                        }
                        else scroll(selectedMenu, --selectedMenu);

                    } else if (event.getCode().equals(KeyCode.DOWN)) {
                        if (selectedMenu == menus.size() - 1) {
                            scroll(menus.size() - 1, 0);
                            selectedMenu = 0;
                        } else scroll(selectedMenu == -1 ? menus.size() - 1 : selectedMenu, ++selectedMenu);
                    } else if (event.getCode().equals(KeyCode.ENTER) && selectedMenu != -1) {
                        UtilAndConstants.fireMouse(menus.get(selectedMenu), MouseEvent.MOUSE_CLICKED);
                    }
                }
            });
            setVisible(true);
        }

        private void scroll(int oldIndex, int newIndex) {
            UtilAndConstants.fireMouse(menus.get(newIndex), MouseEvent.MOUSE_ENTERED);
        }

        public void enter() {
            in.play();
        }

        public void disappear() {
            if (selectedMenu != -1)
                UtilAndConstants.fireMouse(menus.get(selectedMenu), MouseEvent.MOUSE_EXITED);
            selectedMenu = -1;
            out.play();
        }

        public void setColor(Color color) {
            this.color = color;
            setStyle("-fx-background-color: " + UtilAndConstants.colorToHex(color));
            for (Menu m : menus) {
                m.setColor(color);
            }
        }

        class Menu extends AnchorPane {

            Text text;
            Color color;

            public Menu (String text) {
                color = Root.getActiveUser().getAccentColor();
                String colorHex = UtilAndConstants.colorToHex(color);
                setStyle("-fx-background-color: " + colorHex);
                setPadding(new Insets(15));
                setPrefWidth(200);
                Text prompt = new Text(text);
                Color textFill = UtilAndConstants.textFill(color, 2);
                prompt.setFill(textFill);
                prompt.setFont(Font.font("Comfortaa", FontWeight.NORMAL, 20));
                this.getChildren().add(prompt);
                this.text = prompt;
                setAlignment(Pos.CENTER);

                addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
                    for (Menu m: menus) {
                        m.setStyle("-fx-background-color: " + UtilAndConstants.colorToHex(color));
                    }
                    setStyle("-fx-background-color: " + UtilAndConstants.colorToHex(UtilAndConstants.highlightColor(color)));
                    selectedMenu = menus.indexOf(this);
                });
                addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
                    setStyle("-fx-background-color: " + UtilAndConstants.colorToHex(color));
                });
                AnchorPane.setTopAnchor(prompt, 8.5);
                AnchorPane.setLeftAnchor(prompt, 5.0);
            }

            public void setColor(Color c) {
                this.color = c;
                    setStyle("-fx-background-color: " + UtilAndConstants.colorToHex(c));
                    text.setFill(UtilAndConstants.textFill(c));
            }

            public void setText(String txt) {
                text.setText(txt);
            }

            public Text getText() {return text;}
        }
    }
}