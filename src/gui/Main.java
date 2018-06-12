package gui;

import javafx.animation.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.ColorAdjust;
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

    private BarMenu[] menus = new BarMenu[5];
    private int currentMenu = 0;
    private Text subtitle;
    private boolean caps = Toolkit.getDefaultToolkit().getLockingKeyState(java.awt.event.KeyEvent.VK_CAPS_LOCK);


    public static final int BASE_STATE = 0;
    public static final int SLEEP_STATE = 1;
    public static final int TERMINAL_STATE = 2;
    public static final int SIDEBAR_STATE = 3;

    private Node top_bar;
    private Node sleepBody;
    private Node mainBody;
    private Text clock;
    private Text date;
    private Node picture;
    private Terminal term;
    private StackPane mainArea;
    private SideBar sideBar;
    private Text temperature;
    private Text weatherDesc;
    private static final DecimalFormat tempFormat = new DecimalFormat("#00.0");
    private WeatherManager manager;
    private Integer state = 0;
    private Stage primaryStage;
    private Timeline weatherParticleTimer;
    private Timer lightningTimer;
    private ImageView background;
    private AnchorPane weatherPane;
    private boolean day;

    public static void main(String[] args) {
        launch(args);
    }

    public static DecimalFormat getTempFormat() {
        return tempFormat;
    }

    public void newUser() {
        getPrimaryStage().setMaximized(false);
        Scene window = NewUserWindow.get(this);
        getPrimaryStage().setScene(window);
        getPrimaryStage().show();
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
        getPrimaryStage().setMaximized(false);
        getPrimaryStage().setTitle("Welcome, " + Root.getActiveUser().getFirst() + " - Paintbrush LMS");
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
        getMenus()[0] = new BarMenu("latest", 0);
        getMenus()[1] = new BarMenu("classes", 1);
        getMenus()[2] = new BarMenu("organizations", 2);
        getMenus()[3] = new BarMenu("browse lessons", 3);
        getMenus()[4] = new BarMenu("community", 4);
        BarMenu name = new BarMenu(Root.getActiveUser() == null || Root.getActiveUser().getUsername() == null ? "Not signed in" : Root.getActiveUser().getFirst() + " " + Root.getActiveUser().getLast(), -1);
        for (BarMenu m: getMenus()
             ) {
                m.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    if (getState() == SIDEBAR_STATE)
                        UtilAndConstants.fireMouse(name, MouseEvent.MOUSE_CLICKED);
                    scrollBody(m.scrollPos, getSubtitle());
                });
        }
        getMenus()[0].setFont(Font.font(getMenus()[0].getFont().getFamily(), FontWeight.BOLD, getMenus()[0].getFont().getSize()));

        name.setOnMouseClicked(event -> {
            if (getState() == BASE_STATE) {
                name.setFont(Font.font(name.getFont().getFamily(), FontWeight.BOLD, name.getFont().getSize()));
                getSideBar().enter();
                state = SIDEBAR_STATE;
            }
            else if (getState() == SIDEBAR_STATE) {
                name.setFont(Font.font(name.getFont().getFamily(), FontWeight.NORMAL, name.getFont().getSize()));
                getSideBar().disappear();
                state = BASE_STATE;
            }
        });

        Image image = Root.getActiveUser().getAcctImage();
        Shape picture = new ShapeImage(new Circle(30), image).apply();
        this.picture = picture;
        HBox topbar = new HBox(titles, getMenus()[0], getMenus()[1], getMenus()[2], getMenus()[3], getMenus()[4], new UtilAndConstants.Filler(), name, picture);
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
        weatherPane = new AnchorPane();
        manager = new WeatherManager(Root.getActiveUser().getZipcode());
        getManager().update();
        Timer weatherUpdateTimer = new Timer();
        weatherUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateWeather();
            }
        }, 3000000, 300000);
        temperature = new Text();
        boolean temperature_nullcheck = getManager().getTempCelsius() == null;
        if (temperature_nullcheck ) {
            getTemperature().setText("----" + (char) 0x00B0 + (Root.getActiveUser().usesFahrenheit() ? "F" : "C"));
        } else {
            if (Root.getActiveUser().usesFahrenheit()) {
                getTemperature().setText(getTempFormat().format(getManager().getTempFahrenheit()) + (char) 0x00B0 + "F");
            } else {
                getTemperature().setText(getTempFormat().format(getManager().getTempCelsius()) + (char) 0x00B0 + "C");
            }
        }
        getTemperature().setFill(Color.WHITE);
        getTemperature().setFont(Font.font("Sans Serif", FontWeight.NORMAL, 100));
        getTemperature().setTextAlignment(TextAlignment.RIGHT);
        weatherDesc = new Text(getManager().getDescription());
        getWeatherDesc().setFill(Color.WHITE);
        getWeatherDesc().setFont(Font.font("Sans Serif", FontWeight.NORMAL, 45));
        getWeatherDesc().setTextAlignment(TextAlignment.RIGHT);
        VBox weatherDetails = new VBox(getWeatherDesc());
        weatherDetails.setAlignment(Pos.CENTER_RIGHT);
        VBox weatherDisplay = new VBox(getTemperature(), weatherDetails);
        weatherDisplay.setAlignment(Pos.CENTER_RIGHT);
        HBox sleep_btm = new HBox(new VBox(clock,date), new UtilAndConstants.Filler(), weatherDisplay);
        sleep_btm.setAlignment(Pos.BOTTOM_LEFT);

        //synthesize sleep body
        sleepbody.setBottom(sleep_btm);
        sleepbody.setPadding(new Insets(30));
        sleepbody.setVisible(false);

        BorderPane body = new BorderPane();
        mainBody = body;
        ImageView backgd = new ImageView();
        this.background = backgd;
        backgd.setFitWidth(1900);
        backgd.setPreserveRatio(true);
        setWeatherGraphics(background, weatherPane);
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
            if (getState() == TERMINAL_STATE && event.getTarget() == terminalpane)
                quitTerminal();
            else term.current.requestFocus();
        });
        term.setVisible(false);

        StackPane mainArea = new StackPane(backgd, weatherPane, terminalpane, root);
        this.mainArea = mainArea;
        getPrimaryStage().setScene(new Scene(mainArea, 999, 649));
        getPrimaryStage().setMaximized(true);

        //sidebar
        sideBar = new SideBar(this);
        allBodyPanes.getChildren().add(getSideBar());
        body.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getTarget() != getSideBar() && getState() == SIDEBAR_STATE) {
                UtilAndConstants.fireMouse(name, MouseEvent.MOUSE_CLICKED);
                state = BASE_STATE;
            }
        });

        picture.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            UtilAndConstants.fireMouse(name, MouseEvent.MOUSE_CLICKED);
        });

        getPrimaryStage().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (getState() == SLEEP_STATE)
                wakeup();
        });


        getPrimaryStage().getScene().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (getState() == SLEEP_STATE) {
                if (event.getCode().equals(KeyCode.ESCAPE))
                    return;
                wakeup();
                if (event.getCode().equals(KeyCode.CAPS)) {
                    caps = !isCaps();
                }
                return;
            }
            if (event.getCode().equals(KeyCode.SPACE)) {

                if (getState() == BASE_STATE && event.isControlDown()) {
                    UtilAndConstants.fireMouse(name, MouseEvent.MOUSE_CLICKED);
                }

                else if (getState() == BASE_STATE || getState() == SIDEBAR_STATE && !event.isControlDown()) {
                    if (getState() == SIDEBAR_STATE)
                        UtilAndConstants.fireMouse(name, MouseEvent.MOUSE_CLICKED);
                    term.setVisible(true);
                    term.start();
                    state = TERMINAL_STATE;
                    ObservableList<Node> workingCollection = FXCollections.observableArrayList(mainArea.getChildren());
                    Collections.swap(workingCollection, 2, 3);
                    mainArea.getChildren().setAll(workingCollection);
                }

                else if (getState() == SIDEBAR_STATE) {
                    UtilAndConstants.fireMouse(name, MouseEvent.MOUSE_CLICKED);
                }
            }

            if (event.getCode().equals(KeyCode.ESCAPE)) {
                if (getState() == BASE_STATE) {
                    sleep();
                }
                else if (getState() == TERMINAL_STATE) {
                    if (event.isControlDown()) {
                        term.clearTerminal();
                    }
                    quitTerminal();
                } else if (getState() == SIDEBAR_STATE) {
                    UtilAndConstants.fireMouse(name, MouseEvent.MOUSE_CLICKED);
                }
            }
        });

        getPrimaryStage().getScene().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode().equals(KeyCode.LEFT) && getState() == BASE_STATE) {
                if (getCurrentMenu() != 0)
                    scrollBody(getCurrentMenu() - 1, getSubtitle());
            }
            if (event.getCode().equals(KeyCode.RIGHT) && getState() == BASE_STATE) {
                if (getCurrentMenu() != getMenus().length - 1)
                    scrollBody(getCurrentMenu() + 1, getSubtitle());
            }
            if (event.getCode().equals(KeyCode.CAPS)) {
                caps = Toolkit.getDefaultToolkit().getLockingKeyState(java.awt.event.KeyEvent.VK_CAPS_LOCK);
                mainlogo.setText(isCaps() ? upper : lower);
                if (isCaps())
                    getSubtitle().setText("Live your life in ALL CAPS today.");
                else {
                    getSubtitle().setText(getSubtitles()[getCurrentMenu()]);
                }
            }
        });

        state = BASE_STATE;
        getPrimaryStage().show();
    }



    public void updateWeather() {
        getManager().update();
        updateWeatherDisplay();
    }

    public void updateWeatherDisplay() {
        if (Root.getActiveUser().usesFahrenheit()) {
            getTemperature().setText(getTempFormat().format(getManager().getTempFahrenheit()) + (char) 0x00B0 + "F");
        } else {
            getTemperature().setText(getTempFormat().format(getManager().getTempCelsius()) + (char) 0x00B0 + "C");
        }
        getWeatherDesc().setText(getManager().getDescription());
        setWeatherGraphics(background, weatherPane);
    }

    private void setWeatherGraphics(ImageView backgd, AnchorPane weatherPane) {
        String day_partly_cloudy = "background.jpeg";
        String day_sunny = "Day_Sunny.png";
        String day_cloudy = "Day_Cloudy.png";
        String night_clear = "Night_Clear.png";
        String night_cloudy = "Night_Cloudy.png";

        Integer currentHr = Integer.parseInt(new SimpleDateFormat("H").format(new Date(System.currentTimeMillis())));
        Boolean isDaytime = currentHr > 6 && currentHr < 21;
        day = isDaytime;
        String clear_now = isDaytime ? day_sunny : night_clear;
        String cloudy_now = isDaytime ? day_cloudy : night_cloudy;
        String partly_cloudy_now = isDaytime ? day_partly_cloudy : night_cloudy;

        //reset weatherpane
        weatherPane.getChildren().removeAll();
        if (weatherParticleTimer != null)
            weatherParticleTimer.stop();
        if (lightningTimer != null)
            lightningTimer.cancel();
        if (backgd == null)
            return;
        backgd.setEffect(null);

        boolean needMoon = false;
        switch (getManager().getCurrent()) {
            case Fog:
                backgd.setImage(parseBackgroundImage(cloudy_now));
                fog(backgd);
                break;
            case Snow:
                backgd.setImage(parseBackgroundImage(cloudy_now));
                snow(weatherPane, 75);
                break;
            case Blizzard:
                backgd.setImage(parseBackgroundImage(cloudy_now));
                snow(weatherPane, 200);
                break;
            case Thunderstorm:
                lightning(backgd, .167); //no break
            case Heavy_Rain:
                rain(weatherPane, 800);
                backgd.setImage(parseBackgroundImage(cloudy_now));
                break;
            case Light_Rain:
                rain(weatherPane, 400); //no break;
            case Cloudy:
                backgd.setImage(parseBackgroundImage(cloudy_now));
                break;
            case Partly_Cloudy:
                backgd.setImage(parseBackgroundImage(partly_cloudy_now));
                break; //day_partly_cloudy is default image.
            case Sunny:
                backgd.setImage(parseBackgroundImage(clear_now));
                if (!day) needMoon = true;
                break;
            default:
                break;
        }
        if (needMoon) {
            int hourOfNight = (currentHr + 3) % 24 + 1;
            Image moon = new Image("file:" + Address.root_addr + File.separator + "resources" + File.separator + "Moon_Unsoftened.png");
            ImageView moonNode = new ImageView(moon);
            moonNode.setFitWidth(50);
            moonNode.setFitHeight(50);
            double xradius = 800;
            double yradius = 500;
            double dx = Math.cos(Math.PI / 10 * hourOfNight);
            double dy = Math.sin(Math.PI / 10 * hourOfNight);

            double dx_pixels = dx * xradius;
            double dy_pixels = dy * yradius;

            double centerX = 1920/2;
            double centerY = 1080/2;

            AnchorPane.setLeftAnchor(moonNode, centerX + dx_pixels);
            AnchorPane.setTopAnchor(moonNode, 1080 - (centerY + dy_pixels));
            weatherPane.getChildren().add(moonNode);
        }
    }

    private Image parseBackgroundImage(String fn) {
        return new Image("file:" + Address.root_addr + File.separator + "resources" + File.separator + fn);
    }

    private void fog(ImageView background) {
        ColorAdjust colorInput = new ColorAdjust();
        colorInput.setBrightness(.5);
        colorInput.setContrast(-.5);
        if (day)
            background.setEffect(colorInput);
    }

    private void lightning(ImageView background, double flashesPerSecond) {
        ColorAdjust flash = new ColorAdjust();
        Timer flashTimer = new Timer();
        lightningTimer = flashTimer;
        final Random rand = new Random();
        flash.setBrightness(-.3);
        background.setEffect(flash);
        flashTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Timeline pulser = new Timeline(new KeyFrame(Duration.millis(40), event -> {
                    flash.setBrightness(flash.getBrightness() == -.3 ? .7 : -.3);
                    background.setEffect(flash);
                }));
                pulser.setCycleCount((rand.nextInt(5) + 1) * 2);
                pulser.play();
            }
        }, (long) (1000.0/flashesPerSecond), (long) (1000.0 / flashesPerSecond));
    }

    private void rain(AnchorPane weatherPane, int particlesPerSecond) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.0 / particlesPerSecond), event -> weatherPane.getChildren().add(new RainParticle(weatherPane).shape)));
        timeline.setCycleCount(Animation.INDEFINITE);
        weatherParticleTimer = timeline;
        timeline.play();
    }

    private void snow(AnchorPane weatherPane, int particlesPerSecond) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.0 / particlesPerSecond), event -> weatherPane.getChildren().add(new SnowParticle(weatherPane).shape)));
        timeline.setCycleCount(Animation.INDEFINITE);
        weatherParticleTimer = timeline;
        timeline.play();
    }

    private void updateTime() {
        Date date = new Date();
        if (getState() == SLEEP_STATE) {
            String time;
            if (Root.getActiveUser().isClock24Hour())
                time = new SimpleDateFormat( "EEEEEEEE, MMMMMMMMM d, YYYY  H:mm:ss").format(date);
            else
                time = new SimpleDateFormat( "EEEEEEEE, MMMMMMMMM d, YYYY  h:mm:ss aa").format(date);
            String[] timeanddate = time.split("  ");
            getClock().setText(timeanddate[1]);
            getDate().setText(timeanddate[0]);
        }
        int hour = Integer.parseInt(new SimpleDateFormat("H").format(date));
        boolean isDaytime = hour > 6 && hour < 21;
        if (day &&  !isDaytime || !day && isDaytime) {
            setWeatherGraphics(background, weatherPane);
        }
    }

    private void wakeup() {
        state = BASE_STATE;
        getTop_bar().setVisible(true);
        FadeTransition fadein = new FadeTransition(Duration.millis(200), getTop_bar());
        fadein.setFromValue(0);
        fadein.setToValue(1);
        fadein.play();
        FadeTransition fadein_ = new FadeTransition(Duration.millis(200), getMainBody());
        fadein_.setFromValue(0);
        fadein_.setToValue(1);
        fadein_.play();
        getSleepBody().setVisible(false);
    }

    private void sleep() {
        state = SLEEP_STATE;
        getTop_bar().setVisible(false);
        getMainBody().setVisible(false);
        FadeTransition ft = new FadeTransition(Duration.millis(200), getSleepBody());
        ft.setFromValue(0);
        ft.setToValue(1);
        updateTime();
        getSleepBody().setVisible(true);
        ft.play();
    }

    public void clearStage() {
        getPrimaryStage().setScene(null);
    }

    public void resetMain() {
        clearStage();
        getPrimaryStage().setMaximized(false);
        switchToMain();
    }

    public BarMenu[] getMenus() {
        return menus;
    }

    public int getCurrentMenu() {
        return currentMenu;
    }

    public Text getSubtitle() {
        return subtitle;
    }

    public boolean isCaps() {
        return caps;
    }

    public Node getTop_bar() {
        return top_bar;
    }

    public Node getSleepBody() {
        return sleepBody;
    }

    public Node getMainBody() {
        return mainBody;
    }

    public Text getClock() {
        return clock;
    }

    public Text getDate() {
        return date;
    }

    public Node getPicture() {
        return picture;
    }

    public Terminal getTerm() {
        return term;
    }

    public StackPane getMainArea() {
        return mainArea;
    }

    public SideBar getSideBar() {
        return sideBar;
    }

    public Text getTemperature() {
        return temperature;
    }

    public Text getWeatherDesc() {
        return weatherDesc;
    }

    public WeatherManager getManager() {
        return manager;
    }

    public Integer getState() {
        return state;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public String[] getSubtitles() {
        return subtitles;
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

    private String[] subtitles = {
            "Let's get something done today.",
            "Let's learn something today.",
            "Let's do something we love today.",
            "Let's find a new interest today.",
            "Let's see what the world did today.",
    };

    private void scrollBody(int scrollPos, Text changeText) {
        changeText.setText(getSubtitles()[scrollPos]);
        currentMenu = scrollPos;
        BarMenu m = getMenus()[scrollPos];
        for (BarMenu m1 :
                getMenus()) {
            m1.setFont(Font.font(m.getFont().getFamily(), FontWeight.NORMAL, m.getFont().getSize()));
        }
        m.setFont(Font.font(m.getFont().getFamily(), FontWeight.BOLD, m.getFont().getSize()));
    }

    void quitTerminal() {
        getTerm().exit();
        state = BASE_STATE;
        ObservableList<Node> workingCollection = FXCollections.observableArrayList(getMainArea().getChildren());
        Collections.swap(workingCollection, 2, 3);
        getMainArea().getChildren().setAll(workingCollection);
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
            
            openTerminal.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> getPrimaryStage().getScene().getRoot().fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, " ", " ", KeyCode.SPACE, false, false, false, false)));

            grades.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                //TODO
            });

            attendance.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                //TODO
            });

            accountSettings.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (Root.getActiveUser() != null && Root.getActiveUser().getUsername() != null) {
                    UtilAndConstants.fireMouse(Main.this.getPicture(), MouseEvent.MOUSE_CLICKED);
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
                Main.this.getPrimaryStage().setMaximized(false);
                Root.saveAll();
                Main.this.newUser();
            });

            save.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                Main.this.stop();
            });
            getChildren().addAll(menus);
            setAlignment(Pos.TOP_CENTER);
            getPrimaryStage().getScene().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
                if (getState() == SIDEBAR_STATE) {
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