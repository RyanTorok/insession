package gui;

import classes.ClassPd;
import classes.Course;
import classes.Record;
import javafx.animation.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
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
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.Root;
import main.Size;
import main.User;
import main.UtilAndConstants;
import searchengine.Index;
import searchengine.Indexable;
import searchengine.QueryEngine;
import terminal.Address;

import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Main extends Application {

    private BarMenu[] menus = new BarMenu[5];
    private int currentMenu = 0;
    private Text subtitle;
    private boolean caps = Toolkit.getDefaultToolkit().getLockingKeyState(java.awt.event.KeyEvent.VK_CAPS_LOCK);

    public static final int BASE_STATE = 0;
    public static final int SLEEP_STATE = 1;
    public static final int TERMINAL_STATE = 2;
    public static final int SIDEBAR_STATE = 3;
    public static final int SEARCH_STATE = 4;

    private Pane[] contentPanes;
    private HBox contentPanesWrapper;

    private HBox top_bar;
    Line topBarScrollBar;
    private boolean tbsbDrag = false;
    private Node sleepBody;
    private Pane mainBody;
    private StackPane mainBodyAndTaskViews;
    private VBox titles;
    private Text clock;
    private Text date;
    private Text name;
    private Shape picture;
    private int topbarPictureIndex = -1;
    private Terminal terminal;
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
    private StackPane topbarWrapper;

    //Task Views
    private TaskViewWrapper taskViews;
    private boolean homeScreen;

    //Search Interface
    private SearchModule searchBox;
    private StackPane allMenusAndSearchBar;

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
        try {
            this.primaryStage = primaryStage;
            Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
            Root.getUtilAndConstants().setScreenWidth(visualBounds.getWidth());
            Root.getUtilAndConstants().setScreenHeight(visualBounds.getHeight());
            contentPanes = new Pane[5];
            Root.setPortal(this);
            User user = User.read();
            String icon_path = "file:" + Address.root_addr + File.separator + "resources" + File.separator + "icon.png";
            Image iconImg = new Image(icon_path);
            primaryStage.getIcons().add(0, iconImg);
            primaryStage.setTitle("Paintbrush LMS");
            Root.setActiveUser(user);
            if (user == null || User.getSerCount() > 1) {
                newUser();
            } else {
                try {
                    switchToMain();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchToMain() {
        getPrimaryStage().setMaximized(false);
        String title_name = Root.getActiveUser() != null && Root.getActiveUser().getFirst() != null ? Root.getActiveUser().getFirst() : "Guest";
        getPrimaryStage().setTitle("Welcome, " + title_name + " - Paintbrush LMS");
        Text mainlogo = new Text("paintbrush.    ");
        mainlogo.setFont(Font.font("Comfortaa", Size.fontSize(60)));
        mainlogo.setFill(Color.WHITE);
        final String upper = mainlogo.getText().substring(0, mainlogo.getText().length() - 2).toUpperCase();
        final String lower = mainlogo.getText();

        Text subText = new Text("Let's get something done today.");
        subtitle = subText;
        subText.setFont(Font.font("Sans Serif", FontPosture.ITALIC,  Size.fontSize(20)));
        subText.setFill(Color.WHITE);
        titles = new VBox(mainlogo, subText);
        titles.setSpacing(5);
        titles.setPadding(Size.insets(10, 0, 0, 0));

        //scroll links
        getMenus()[0] = new BarMenu("Latest", 0);
        getMenus()[1] = new BarMenu("Classes", 1);
        getMenus()[2] = new BarMenu("Organizations", 2);
        getMenus()[3] = new BarMenu("Browse Lessons", 3);
        getMenus()[4] = new BarMenu("Community", 4);
        BarMenu name = new BarMenu(Root.getActiveUser() == null || Root.getActiveUser().getUsername() == null ? "Not signed in" : Root.getActiveUser().getFirst() + " " + Root.getActiveUser().getLast(), -1);
        this.setName(name);
        for (BarMenu m : getMenus()
                ) {
            m.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (getState() == SIDEBAR_STATE)
                    UtilAndConstants.fireMouse(name, MouseEvent.MOUSE_CLICKED);
                scrollBody(m.scrollPos, getSubtitle());
            });
        }
        getMenus()[0].setFont(Font.font(getMenus()[0].getFont().getFamily(), FontPosture.ITALIC, getMenus()[0].getFont().getSize()));

        name.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (state == SEARCH_STATE)
                closeSearchBar();
            if (getState() == BASE_STATE) {
                name.setFont(Font.font(name.getFont().getFamily(), FontWeight.BOLD, name.getFont().getSize()));
                getSideBar().enter();
                state = SIDEBAR_STATE;
            } else if (getState() == SIDEBAR_STATE) {
                sideBar.requestFocus();
                name.setFont(Font.font(name.getFont().getFamily(), FontWeight.NORMAL, name.getFont().getSize()));
                getSideBar().disappear();
                state = BASE_STATE;
            }
        });

        Image image = Root.getActiveUser().getAcctImage();
        Shape picture = new ShapeImage(new Circle(Size.lessWidthHeight(30)), image).apply();
        this.picture = picture;
        HBox menusWrapper = new HBox(menus[0], menus[1], menus[2], menus[3], menus[4]);
        menusWrapper.setAlignment(Pos.CENTER_LEFT);
        menusWrapper.setSpacing(Size.width(30));
        allMenusAndSearchBar = new StackPane(menusWrapper);
        HBox topbar = new HBox(titles, allMenusAndSearchBar, new UtilAndConstants.Filler(), new AnchorPane(name), new AnchorPane(picture));
        AnchorPane.setTopAnchor(name, Size.height(40));
        AnchorPane.setLeftAnchor(picture, Size.width(5));
        AnchorPane.setTopAnchor(picture, Size.height(22.5));
        topbarPictureIndex = topbar.getChildren().size() - 1; //picture is last item in top bar.
        top_bar = topbar;
        topbar.setSpacing(Size.width(35));
        topbar.setAlignment(Pos.TOP_LEFT);
        String color = UtilAndConstants.colorToHex(Root.getActiveUser().getAccentColor());
        String borderWidth = ".67em";
        topbar.setStyle("-fx-background-color: #000000; -fx-border-color: " + color + "; -fx-border-width: 0em 0em " + borderWidth + " 0em; -fx-border-style: solid");
        topbar.setPadding(Size.insets(15));
        top_bar.setPrefHeight(Size.height(135));
        top_bar.setMinHeight(Size.height(135));

        //top bar scroll bar
        topBarScrollBar = new Line();
        topBarScrollBar.setStartX(0);
        topBarScrollBar.setEndX(Size.width(75));
        topBarScrollBar.setStartY(0);
        topBarScrollBar.setEndY(topBarScrollBar.getStartY());
        topBarScrollBar.setStrokeWidth(Size.lessWidthHeight(8));
        topBarScrollBar.setStroke(UtilAndConstants.highlightColor(Root.getActiveUser().getAccentColor()));

        top_bar.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (state == SEARCH_STATE)
                return;
            //don't trigger drags on name or picture
            if (event.getTarget() == topbar.getChildren().get(topbarPictureIndex) || event.getTarget() == name || event.getTarget() == ((AnchorPane) topbar.getChildren().get(topbarPictureIndex)).getChildren().get(0))
                return;
            topBarScrollBar.setTranslateX(event.getSceneX());
            contentPanesWrapper.setTranslateX(Size.width(Math.max(-4 * 1920, Math.min(0, -5 * (event.getSceneX() - 450) * 4))));
            tbsbDrag = true;
        });

        top_bar.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if (!tbsbDrag && event.getTarget() != topbar && event.getTarget() != menusWrapper && event.getTarget() != allMenusAndSearchBar)
                return;
            tbsbDrag = false;
            double endX = event.getSceneX();
            int closestIndex = -1;
            double closestDistance = Double.MAX_VALUE;
            int i = 0;
            for (BarMenu m : menus) {
                double menuX = allMenusAndSearchBar.getLayoutX() + menusWrapper.getLayoutX() + m.getLayoutX();
                double distance = Math.abs(endX - menuX);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestIndex = i;
                }
                i++;
            }
            scrollBody(closestIndex, subText);
        });

        topbarWrapper = new StackPane(topbar, topBarScrollBar);
        topbarWrapper.setAlignment(Pos.BOTTOM_LEFT);

        //clock
        final Text clock = new Text("");
        this.clock = clock;
        final Text date = new Text("");
        this.date = date;
        clock.setFill(Color.WHITE);
        clock.setFont(Font.font("Sans Serif", FontWeight.NORMAL, Size.fontSize(100)));
        date.setFill(Color.WHITE);
        date.setFont(Font.font("Sans Serif", FontWeight.NORMAL, Size.fontSize(45)));
        Timeline clockTimeline = new Timeline(new KeyFrame(Duration.millis(500), event -> Main.this.updateTime()));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
        BorderPane sleepbody = new BorderPane();
        sleepBody = sleepbody;

        //weather display
        weatherPane = new AnchorPane();
        manager = new WeatherManager(Root.getActiveUser().getZipcode());
        getManager().update();
        Timeline weatherUpdateTimer = new Timeline(new KeyFrame(Duration.millis(300000), event -> updateWeather()));
        weatherUpdateTimer.setCycleCount(Animation.INDEFINITE);
        weatherUpdateTimer.play();
        temperature = new Text();
        setTemperatureDisplay();
        getTemperature().setFill(Color.WHITE);
        getTemperature().setFont(Font.font("Sans Serif", FontWeight.NORMAL, Size.fontSize(100)));
        getTemperature().setTextAlignment(TextAlignment.RIGHT);
        weatherDesc = new Text(getManager().getDescription());
        getWeatherDesc().setFill(Color.WHITE);
        getWeatherDesc().setFont(Font.font("Sans Serif", FontWeight.NORMAL, Size.fontSize(45)));
        getWeatherDesc().setTextAlignment(TextAlignment.RIGHT);
        VBox weatherDetails = new VBox(getWeatherDesc());
        weatherDetails.setAlignment(Pos.CENTER_RIGHT);
        VBox weatherDisplay = new VBox(getTemperature(), weatherDetails);
        weatherDisplay.setAlignment(Pos.CENTER_RIGHT);
        HBox sleep_btm = new HBox(new VBox(clock, date), new UtilAndConstants.Filler(), weatherDisplay);
        sleep_btm.setAlignment(Pos.BOTTOM_LEFT);

        //synthesize sleep body
        sleepbody.setBottom(sleep_btm);
        sleepbody.setPadding(Size.insets(30));
        sleepbody.setVisible(false);

        BorderPane body = new BorderPane();
        mainBody = body;
        ImageView backgd = new ImageView();
        this.background = backgd;
        backgd.setFitWidth(Size.width(1900));
        backgd.setPreserveRatio(true);
        setWeatherGraphics(background, weatherPane);
        mainBodyAndTaskViews = new StackPane();
        StackPane allBodyPanes = new StackPane(sleepbody, mainBodyAndTaskViews);
        VBox root = new VBox(topbarWrapper, allBodyPanes);
        root.setMinHeight(Size.height(1080));
        ScrollPane terminalWrapper = new ScrollPane();
        Terminal term = new Terminal(this, terminalWrapper);
        this.terminal = term;
        terminalWrapper.setContent(term);
        terminalWrapper.setFitToHeight(true);
        terminalWrapper.setStyle("-fx-background-color: #202020");
        terminalWrapper.setPrefWidth(term.getPrefWidth());
        terminalWrapper.setPrefHeight(term.getPrefHeight());
        terminalWrapper.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        terminalWrapper.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        terminalWrapper.setFitToWidth(true);
        AnchorPane terminalpane = new AnchorPane(terminalWrapper);
        terminalpane.setPrefHeight(Size.height(649));
        terminalpane.setPrefWidth(Size.width(999));
        terminalpane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            //not the actual terminal
            if (getState() == TERMINAL_STATE && event.getTarget() == terminalpane)
                quitTerminal();
            else term.current.requestFocus();
        });
        term.setVisible(false);

        StackPane mainArea = new StackPane(backgd, weatherPane, terminalpane, root);
        this.mainArea = mainArea;
        getPrimaryStage().setScene(new Scene(mainArea, Size.width(1920), Size.height(1080)));
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


        //load search index
        Index searchIndex = Index.loadLocal();

        //search box
        searchBox = new SearchModule(new QueryEngine(searchIndex), this);

        primaryStage.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode().equals(KeyCode.SPACE)) {
                if (state == BASE_STATE && !event.isControlDown()) {
                    openSearchBar();
                }
            } else if (event.getCode().equals(KeyCode.ESCAPE)) {
                if (state == SEARCH_STATE) {
                    closeSearchBar();
                }
            }
        });

        allMenusAndSearchBar.getChildren().add(searchBox);
        searchBox.setVisible(false);
        searchBox.setPadding(Size.insets(19, 0, 0, 0));

        //content panes
        contentPanesWrapper = new HBox();
        contentPanesWrapper.setPrefWidth(Size.width(1920 * contentPanes.length));
        contentPanesWrapper.setPrefHeight(Size.height(1080));

        //latest
        GridPane latestGrid = new GridPane();
        latestGrid.setPadding(Size.insets(30));
        contentPanes[0] = latestGrid;
        latestGrid.setHgap(Size.width(50));
        latestGrid.setVgap(Size.height(20));

        ArrayList<Record> updates = Root.getActiveUser().getUpdates();

        List<Record> r_announcements = updates.stream().filter(record -> record.getMenuPlacement() == Record.Sorting.Announcements).collect(Collectors.toList());
        List<Record> r_coming_up = updates.stream().filter(record -> record.getMenuPlacement() == Record.Sorting.Coming_Up).collect(Collectors.toList());
        List<Record> r_notifications = updates.stream().filter(record -> record.getMenuPlacement() == Record.Sorting.Notifications).collect(Collectors.toList());

        LatestPane notifications = new LatestPane("Notifications", r_notifications, "Nothing new here!");
        latestGrid.add(notifications, 0, 0);

        LatestPane announcements = new LatestPane("Announcements", r_announcements, "You're all caught up!");
        latestGrid.add(announcements, 1, 0);

        LatestPane comingUp = new LatestPane("Coming Up", r_coming_up, "Your agenda is clear!");
        latestGrid.add(comingUp, 2, 0);

        GridPane.setHgrow(announcements, Priority.ALWAYS);


        //classes
        GridPane classLauncherGrid = new GridPane();
        contentPanes[1] = classLauncherGrid;

        List<ClassPd> allClasses = new ArrayList<>();
        allClasses.addAll(Root.getActiveUser().getClassesTeacher());
        allClasses.addAll(Root.getActiveUser().getClassesStudent());

        ArrayList<ClassLauncher> launchers = new ArrayList<>();
        classLauncherGrid.setHgap(Size.width(40));
        classLauncherGrid.setVgap(Size.height(40));

        if (allClasses.size() > 0) {

            int numRows = (int) Math.ceil(Math.sqrt(allClasses.size() / 2));

            int launchersPerRow = (allClasses.size() + numRows - 1) / numRows;
            int clWidth = (int) Size.width(1840.0 / launchersPerRow - 2 * classLauncherGrid.getHgap());

            int remainder = (int) Size.height(1900 - ((clWidth + classLauncherGrid.getHgap()) * launchersPerRow + classLauncherGrid.getHgap())) / 2;
            classLauncherGrid.setPadding(Size.insets(classLauncherGrid.getHgap(), classLauncherGrid.getHgap() + remainder, classLauncherGrid.getHgap(), classLauncherGrid.getHgap() + remainder));

            ClassPd test = new ClassPd();
            test.setPeriodNo(1);
            test.setTeacherFirst("FirstName");
            test.setTeacherLast("LastName");
            test.setCastOf(new Course());
            for (int i = 0; i < 7; i++) {
                test.setPeriodNo(new Random().nextInt(10));
                allClasses.set(i, test);
            }

            allClasses.sort(Comparator.comparing(ClassPd::getPeriodNo).thenComparing(ClassPd::getTeacherLast).thenComparing(ClassPd::getTeacherFirst));

            for (int i = 0; i < allClasses.size(); i++) {
                ClassLauncher launcher = new ClassLauncher(allClasses.get(i), clWidth);
                classLauncherGrid.add(launcher, i % launchersPerRow, i / launchersPerRow);
            }
        }

        //organizations


        GridPane organizationsGrid = new GridPane();
        contentPanes[2] = organizationsGrid;

        //browse lessons

        GridPane browseLessonsGrid = new GridPane();
        contentPanes[3] = browseLessonsGrid;

        //community

        GridPane communityGrid = new GridPane();
        contentPanes[4] = communityGrid;

        double cpX = Size.width(1860), cpY = Size.height(1000);

        for (Pane p : contentPanes) {
            p.setMaxSize(cpX, cpY);
            p.setMinSize(cpX, cpY);
        }
        contentPanesWrapper.getChildren().addAll(contentPanes);
        mainBody.getChildren().add(contentPanesWrapper);

        taskViews = new TaskViewWrapper();

        mainBodyAndTaskViews.getChildren().addAll(taskViews, mainBody);
        homeScreen = true;

        picture.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> UtilAndConstants.fireMouse(name, MouseEvent.MOUSE_CLICKED));

        getPrimaryStage().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (getState() == SLEEP_STATE) {
                wakeup();
                event.consume();
            }
        });


        getPrimaryStage().getScene().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (getState() == SLEEP_STATE) {
                if (event.getCode().equals(KeyCode.ESCAPE))
                    return;
                wakeup();
                if (event.getCode().equals(KeyCode.CAPS)) {
                    caps = !isCaps();
                }
                event.consume();
                return;
            }
            if (event.getCode().equals(KeyCode.TAB)) {

                if (getState() == BASE_STATE) {
                    term.setVisible(true);
                    term.start();
                    state = TERMINAL_STATE;
                    ObservableList<Node> workingCollection = FXCollections.observableArrayList(mainArea.getChildren());
                    Collections.swap(workingCollection, 2, 3);
                    mainArea.getChildren().setAll(workingCollection);
                    if (term.current != null)
                        term.current.requestFocus();
                }
            }

            if (event.getCode().equals(KeyCode.SPACE) && event.isControlDown()) {
                if (state == BASE_STATE || state == SIDEBAR_STATE) {
                    UtilAndConstants.fireMouse(name, MouseEvent.MOUSE_CLICKED);
                }
            }

            if (event.getCode().equals(KeyCode.ESCAPE)) {
                if (getState() == BASE_STATE) {
                    sleep();
                } else if (getState() == TERMINAL_STATE) {
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
            if (event.getCode().equals(KeyCode.LEFT) && getState() == BASE_STATE && homeScreen) {
                if (getCurrentMenu() != 0)
                    scrollBody(getCurrentMenu() - 1, getSubtitle());
            }
            if (event.getCode().equals(KeyCode.RIGHT) && getState() == BASE_STATE && homeScreen) {
                if (getCurrentMenu() != getMenus().length - 1)
                    scrollBody(getCurrentMenu() + 1, getSubtitle());
            }
            if (event.getCode().equals(KeyCode.CAPS)) {
                caps = Toolkit.getDefaultToolkit().getLockingKeyState(java.awt.event.KeyEvent.VK_CAPS_LOCK);
                mainlogo.setText(isCaps() ? upper : lower);
                if (isCaps())
                    getSubtitle().setText("Live your life in ALL CAPS today.");
                else {
                    if (state == SEARCH_STATE) {
                        getSubtitle().setText(searchBox.getDescription());
                    } else getSubtitle().setText(getSubtitles()[getCurrentMenu()]);
                }
            }
        });


        state = BASE_STATE;
        getPrimaryStage().show();
        repositionTopBarScrollBar(0, 1);

        for (Collection<Indexable> list :
                QueryEngine.getPrimaryIndexSets()) {
            searchBox.getEngine().getIndex().index(list);
        }
    }

    private void closeSearchBar() {
        allMenusAndSearchBar.getChildren().get(0).setVisible(true);  //show menus
        allMenusAndSearchBar.getChildren().get(1).setVisible(false); //hide search box
        searchBox.collapse();
        subtitle.setText(caps ? "Live life in ALL CAPS today." : subtitles[currentMenu]);
        state = BASE_STATE;
    }


    void updateWeather() {
        getManager().update();
        updateWeatherDisplay();
    }

    void updateWeatherDisplay() {
        setTemperatureDisplay();
        getWeatherDesc().setText(getManager().getDescription());
        setWeatherGraphics(background, weatherPane);
    }

    private void setTemperatureDisplay() {
        if (getManager().getTempCelsius() == null) {
            getTemperature().setText("----" + (char) 0x00B0 + (Root.getActiveUser().usesFahrenheit() ? "F" : "C"));
        } else {
            if (Root.getActiveUser().usesFahrenheit()) {
                getTemperature().setText(getTempFormat().format(getManager().getTempFahrenheit()) + (char) 0x00B0 + "F");
            } else {
                getTemperature().setText(getTempFormat().format(getManager().getTempCelsius()) + (char) 0x00B0 + "C");
            }
        }
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
        weatherPane.getChildren().clear();
        if (weatherParticleTimer != null)
            weatherParticleTimer.stop();
        if (lightningTimer != null)
            lightningTimer.cancel();
        if (backgd == null)
            return;
        backgd.setEffect(null);

        boolean needMoon = false;
        if (manager.getCurrent() == null)
           manager.setCurrent(WeatherState.Sunny);
        switch (getManager().getCurrent()) {
            case Fog:
                backgd.setImage(parseBackgroundImage(cloudy_now));
                fog(backgd);
                break;
            case Fog_And_Snow:
                fog(backgd);
            case Snow:
                backgd.setImage(parseBackgroundImage(cloudy_now));
                snow(weatherPane, 75);
                break;
            case Fog_And_Blizzard:
                fog(backgd);
            case Blizzard:
                backgd.setImage(parseBackgroundImage(cloudy_now));
                snow(weatherPane, 200);
                break;
            case Fog_And_Thunderstorm:
                fog(backgd);
            case Thunderstorm:
                lightning(backgd, .167); //no break
            case Heavy_Rain:
                rain(weatherPane, 800);
                backgd.setImage(parseBackgroundImage(cloudy_now));
                break;
            case Fog_And_Heavy_Rain:
                fog(backgd);
                rain(weatherPane, 800);
                backgd.setImage(parseBackgroundImage(cloudy_now));
                break;
            case Fog_And_Light_Rain:
                fog(backgd);
            case Light_Rain:
                rain(weatherPane, 400); //no break;
            case Cloudy:
                backgd.setImage(parseBackgroundImage(cloudy_now));
                break;
            case Partly_Cloudy:
                backgd.setImage(parseBackgroundImage(partly_cloudy_now));
                break;
            case Sunny:
                backgd.setImage(parseBackgroundImage(clear_now));
                if (!day) needMoon = true;
                break;
            default:
                break;
        }
        if (needMoon) {
            int hourOfNight = (currentHr + 3) % 24 + 1;
            Image moon = new Image("file:" + Address.root_addr + File.separator + "resources" + File.separator + getMoonFN() + ".png");
            ImageView moonNode = new ImageView(moon);
            double moonImgRadius = Size.lessWidthHeight(40);
            moonNode.setFitWidth(moonImgRadius);
            moonNode.setFitHeight(moonImgRadius);
            double xradius = 800;
            double yradius = 500;
            double dx = Math.cos(Math.PI / 10 * hourOfNight);
            double dy = Math.sin(Math.PI / 10 * hourOfNight);

            double dx_pixels = dx * xradius;
            double dy_pixels = dy * yradius;

            double centerX = 1920 / 2;
            double centerY = 1080 / 2;

            AnchorPane.setLeftAnchor(moonNode, Size.width(centerX + dx_pixels));
            AnchorPane.setTopAnchor(moonNode, Size.height(1080 - (centerY + dy_pixels)));
            weatherPane.getChildren().add(moonNode);
        }
    }

    private String getMoonFN() {
        switch (manager.getMoonState()) {
            case New_Moon:
                return "new_moon";
            case Waxing_Crescent:
                return "waxing_crescent";
            case First_Quarter:
                return "first_quarter";
            case Waxing_Gibbous:
                return "waxing_gibbous";
            case Full_Moon:
                return "full_moon";
            case Waning_Gibbous:
                return "waning_gibbous";
            case Last_Quarter:
                return "last_quarter";
            case Waning_Crescent:
                return "waning_crescent";
            default:
                return "new_moon";
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
        }, (long) (1000.0 / flashesPerSecond), (long) (1000.0 / flashesPerSecond));
    }

    private void rain(AnchorPane weatherPane, int particlesPerSecond) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.0 / particlesPerSecond), event -> weatherPane.getChildren().add(new RainParticle(weatherPane, day).shape)));
        timeline.setCycleCount(Animation.INDEFINITE);
        weatherParticleTimer = timeline;
        timeline.play();
    }

    private void snow(AnchorPane weatherPane, int particlesPerSecond) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.0 / particlesPerSecond), event -> weatherPane.getChildren().add(new SnowParticle(weatherPane, day).shape)));
        timeline.setCycleCount(Animation.INDEFINITE);
        weatherParticleTimer = timeline;
        timeline.play();
    }

    private void updateTime() {
        Date date = new Date();
        if (getState() == SLEEP_STATE) {
            String time;
            if (Root.getActiveUser().isClock24Hour())
                time = new SimpleDateFormat("EEEEEEEE, MMMMMMMMM d, YYYY  H:mm:ss").format(date);
            else
                time = new SimpleDateFormat("EEEEEEEE, MMMMMMMMM d, YYYY  h:mm:ss aa").format(date);
            String[] timeanddate = time.split("  ");
            getClock().setText(timeanddate[1]);
            getDate().setText(timeanddate[0]);
        }
        int hour = Integer.parseInt(new SimpleDateFormat("H").format(date));
        int newHrChecksum = Integer.parseInt(new SimpleDateFormat("mmss").format(date));
        boolean isDaytime = hour > 6 && hour < 21;
        if (day && !isDaytime || !day && isDaytime || newHrChecksum == 0 && !day) {
            setWeatherGraphics(background, weatherPane);
        }
    }

    private void wakeup() {
        state = BASE_STATE;
        topbarWrapper.setVisible(true);
        FadeTransition fadein = new FadeTransition(Duration.millis(200), topbarWrapper);
        fadein.setFromValue(0);
        fadein.setToValue(1);
        fadein.play();
        mainBodyAndTaskViews.setVisible(true);
        FadeTransition fadein_ = new FadeTransition(Duration.millis(200), mainBodyAndTaskViews);
        fadein_.setFromValue(0);
        fadein_.setToValue(1);
        fadein_.play();
        getSleepBody().setVisible(false);
    }

    private void sleep() {
        state = SLEEP_STATE;
        topbarWrapper.setVisible(false);
        mainBodyAndTaskViews.setVisible(false);
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

    public HBox getTop_bar() {
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

    public Shape getPicture() {
        return picture;
    }

    public Terminal getTerminal() {
        return terminal;
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

    public void launchClass(ClassPd classPd) {
        assert state == BASE_STATE;
        System.out.println("Launch " + classPd.getCastOf().getName());
    }

    public void setPicture(Shape newShape) {
        this.picture = newShape;
    }

    public Text getName() {
        return name;
    }

    public void setName(Text name) {
        this.name = name;
    }

    public boolean isHomeScreen() {
        return homeScreen;
    }

    public void setHomeScreen(boolean homeScreen) {
        this.homeScreen = homeScreen;
    }

    public void expandTopBar() {
        Timeline expansion = new Timeline(new KeyFrame(Duration.millis(200), new KeyValue(top_bar.prefHeightProperty(), Size.height(1080)), new KeyValue(top_bar.minHeightProperty(), Size.height(1080))));
        expansion.play();
    }

    public void collapseTopBar() {
        Timeline expansion = new Timeline(new KeyFrame(Duration.millis(200), new KeyValue(top_bar.prefHeightProperty(), Size.height(135)), new KeyValue(top_bar.minHeightProperty(), Size.height(135))));
        expansion.play();
    }

    public VBox getTitles() {
        return titles;
    }

    class BarMenu extends Text {
        int scrollPos;

        public BarMenu(String text, int order) {
            super(text);
            scrollPos = order;
            addEventHandler(MouseEvent.MOUSE_ENTERED, event -> this.setUnderline(true));
            addEventFilter(MouseEvent.MOUSE_EXITED, event -> this.setUnderline(false));
            setFont(Font.font("Confortaa", Size.fontSize(20)));
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
        hideTaskViews();
        int oldMenu = currentMenu;
        if (state != SEARCH_STATE)
            changeText.setText(getSubtitles()[scrollPos]);
        currentMenu = scrollPos;
        BarMenu m = getMenus()[scrollPos];
        for (BarMenu m1 :
                getMenus()) {
            m1.setFont(Font.font(m.getFont().getFamily(), FontPosture.REGULAR, m.getFont().getSize()));
        }
        m.setFont(Font.font(m.getFont().getFamily(), FontPosture.ITALIC, m.getFont().getSize()));
        int duration = 200;
        TranslateTransition bodyTransition = new TranslateTransition(Duration.millis(duration), contentPanesWrapper);
        bodyTransition.setToX(Size.width(-1920 * scrollPos));
        bodyTransition.play();

        repositionTopBarScrollBar(scrollPos, duration);
    }

    private void repositionTopBarScrollBar(int scrollPos, int duration) {
        TranslateTransition scrollBarTransition = new TranslateTransition(Duration.millis(duration), topBarScrollBar);
        scrollBarTransition.setToX(menus[scrollPos].getLayoutX() + Size.width(445));
        Timeline growShrink = new Timeline(new KeyFrame(Duration.millis(duration), new KeyValue(topBarScrollBar.endXProperty(), 10 * menus[scrollPos].getText().length())));
        growShrink.setCycleCount(1);
        growShrink.play();
        scrollBarTransition.play();
    }

    public void quitTerminal() {
        getTerminal().exit();
        state = BASE_STATE;
        getTerminal().exit.setOnFinished(event -> {
            ObservableList<Node> workingCollection = FXCollections.observableArrayList(getMainArea().getChildren());
            Collections.swap(workingCollection, 3, 2);
            getMainArea().getChildren().setAll(workingCollection);
        });
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

        SideBar(Main holder) {
            super();
            this.holder = holder;
            setVisible(false);

            setPrefWidth(Size.width(200));
            setPrefHeight(Size.height(1000));
            setStyle("-fx-background-color: " + UtilAndConstants.colorToHex(Root.getActiveUser().getAccentColor()));
            //initial placement
            init = new TranslateTransition();
            init.setNode(this);
            init.setDuration(Duration.millis(1));
            init.setByX(Size.width(1900));
            init.setAutoReverse(false);
            init.play();

            //enter screen animation
            in = new TranslateTransition();
            in.setNode(this);
            in.setDuration(Duration.millis(200));
            in.setToX(Size.width(1600));
            in.setAutoReverse(false);

            //exit screen animation
            out = new TranslateTransition();
            out.setNode(this);
            out.setDuration(Duration.millis(200));
            out.setToX(Size.width(1900));
            out.setAutoReverse(false);

            menus = new ArrayList<>();

            boolean signedIn = Root.getActiveUser() != null && Root.getActiveUser().getUsername() != null;
            Menu openTerminal = new Menu("Open Terminal");
            Menu calendar = new Menu("Calendar");
            Menu grades = new Menu("My Grades");
            Menu attendance = new Menu("Attendance");
            Menu accountSettings = new Menu("Account Settings");
            Menu kbShortcuts = new Menu("Keyboard Shortcuts");
            Menu history = new Menu("Usage History");
            Menu privacy = new Menu("Privacy Policy");
            Menu help = new Menu("Help");
            Menu switch_user = new Menu(signedIn ? "Switch User" : "Sign in");
            Menu save = new Menu(signedIn ? "Save and Exit" : "Exit");

            menus.add(openTerminal);
            menus.add(calendar);
            menus.add(grades);
            menus.add(attendance);
            menus.add(accountSettings);
            menus.add(kbShortcuts);
            menus.add(history);
            menus.add(privacy);
            menus.add(help);
            menus.add(switch_user);
            menus.add(save);

            openTerminal.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> getPrimaryStage().getScene().getRoot()
                    .fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, " ", " ", KeyCode.TAB, false, false, false, false)));

            calendar.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                closeSideBar();
                Main.this.launchTaskView(new Calendar());
            });

            grades.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                closeSideBar();
                Main.this.launchTaskView(new Grades());
            });

            attendance.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                closeSideBar();
                Main.this.launchTaskView(new Attendance());
            });

            accountSettings.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                sideBar.requestFocus();
                name.setFont(Font.font(name.getFont().getFamily(), FontWeight.NORMAL, name.getFont().getSize()));
                closeSideBar();
                if (Root.getActiveUser() != null && Root.getActiveUser().getUsername() != null) {
                    UtilAndConstants.fireMouse(Main.this.getPicture(), MouseEvent.MOUSE_CLICKED);
                    new AcctSettings().show();
                }
            });

            history.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                closeSideBar();
                Main.this.launchTaskView(new History());
            });

            privacy.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                closeSideBar();
                Main.this.launchTaskView(new PrivacyPolicy());
            });

            help.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                closeSideBar();
                Main.this.launchTaskView(new Help());
            });

            switch_user.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                Main.this.getPrimaryStage().setMaximized(false);
                Root.saveAll();
                Main.this.newUser();
            });

            save.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> Main.this.stop());
            getChildren().addAll(menus);
            setAlignment(Pos.TOP_CENTER);
            getPrimaryStage().getScene().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
                if (getState() == SIDEBAR_STATE) {
                    if (event.getCode().equals(KeyCode.UP)) {
                        if (selectedMenu == 0 || selectedMenu == -1) {
                            scroll(0, menus.size() - 1);
                            selectedMenu = menus.size() - 1;
                        } else scroll(selectedMenu, --selectedMenu);

                    } else if (event.getCode().equals(KeyCode.DOWN)) {
                        if (selectedMenu == menus.size() - 1) {
                            scroll(menus.size() - 1, 0);
                            selectedMenu = 0;
                        } else scroll(selectedMenu == -1 ? menus.size() - 1 : selectedMenu, ++selectedMenu);
                    } else if (event.getCode().equals(KeyCode.ENTER) && selectedMenu != -1) {
                        UtilAndConstants.fireMouse(menus.get(selectedMenu), MouseEvent.MOUSE_CLICKED);
                    }
                    event.consume();
                }
            });
            setVisible(true);
        }

        private void scroll(int oldIndex, int newIndex) {
            UtilAndConstants.fireMouse(menus.get(newIndex), MouseEvent.MOUSE_ENTERED);
        }

        public void enter() {
            //resolves strange behavior on first load
            selectedMenu = -1;
            in.play();
        }

        @Deprecated
        public void instantDisappear() {
            if (selectedMenu != -1)
                UtilAndConstants.fireMouse(menus.get(selectedMenu), MouseEvent.MOUSE_EXITED);
            selectedMenu = -1;
            Duration old = out.getDuration();
            out.setDuration(Duration.ZERO);
            out.play();
            out.setOnFinished(event -> out.setDuration(old));
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

            Menu(String text) {
                color = Root.getActiveUser().getAccentColor();
                String colorHex = UtilAndConstants.colorToHex(color);
                setStyle("-fx-background-color: " + colorHex);
                setPadding(Size.insets(15));
                setPrefWidth(Size.width(200));
                Text prompt = new Text(text);
                Color textFill = UtilAndConstants.textFill(color, 2);
                prompt.setFill(textFill);
                prompt.setFont(Font.font("Comfortaa", FontWeight.NORMAL, Size.fontSize(20)));
                this.getChildren().add(prompt);
                this.text = prompt;
                setAlignment(Pos.CENTER);

                addEventHandler(MouseEvent.MOUSE_CLICKED, event -> UtilAndConstants.fireMouse(picture, MouseEvent.MOUSE_CLICKED));

                addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
                    for (Menu m : menus) {
                        m.setStyle("-fx-background-color: " + UtilAndConstants.colorToHex(color));
                    }
                    setStyle("-fx-background-color: " + UtilAndConstants.colorToHex(UtilAndConstants.highlightColor(color)));
                    selectedMenu = menus.indexOf(this);
                });
                addEventHandler(MouseEvent.MOUSE_EXITED, event -> setStyle("-fx-background-color: " + UtilAndConstants.colorToHex(color)));
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

            public Text getText() {
                return text;
            }
        }
    }

    public void launchTaskView(TaskView view) {
        assert mainBodyAndTaskViews.getChildren().size() == 2;
        showTaskViews(view);
        taskViews.launch(view);
        homeScreen = false;
    }

    void showTaskViews() {
        showTaskViews(false);
    }

    private void showTaskViews(TaskView newView) {
        if (newView != null)
            showTaskViews(true);
    }

    private void showTaskViews(boolean newView) {
        if (!homeScreen || (taskViews.getActiveViews().size() == 0 && !newView))
            return;
        assert mainBodyAndTaskViews.getChildren().indexOf(taskViews) == 0;
        ObservableList<Node> workingCollection = FXCollections.observableArrayList(mainBodyAndTaskViews.getChildren());
        Collections.swap(workingCollection, 0, 1);
        mainBodyAndTaskViews.getChildren().setAll(workingCollection);
        taskViews.setVisible(true);
        homeScreen = false;
    }

    void hideTaskViews() {
        if (homeScreen)
            return;
        assert mainBodyAndTaskViews.getChildren().indexOf(taskViews) == 1;
        ObservableList<Node> workingCollection = FXCollections.observableArrayList(mainBodyAndTaskViews.getChildren());
        Collections.swap(workingCollection, 0, 1);
        mainBodyAndTaskViews.getChildren().setAll(workingCollection);
        taskViews.setVisible(false);
        homeScreen = true;
    }

    void closeSideBar() {
        if (state != SIDEBAR_STATE)
            return;
        sideBar.selectedMenu = -1;
        sideBar.disappear();
        state = BASE_STATE;
    }

    public TaskViewWrapper getTaskViews() {
        return taskViews;
    }

    public SearchModule getSearchBox() {
        return searchBox;
    }

    public SearchModule openSearchBar() {
        if (state == SEARCH_STATE)
            return searchBox;
        allMenusAndSearchBar.getChildren().get(0).setVisible(false); //hide menus
        allMenusAndSearchBar.getChildren().get(1).setVisible(true);  //show search box
        subtitle.setText("What can I help you find?");
        state = SEARCH_STATE;
        searchBox.getSearchBox().setText("");
        searchBox.getSearchBox().requestFocus();

        //fade in transition
        FadeTransition transition = new FadeTransition(Duration.millis(200));
        transition.setNode(searchBox);
        transition.setFromValue(0);
        transition.setToValue(1);
        transition.play();

        return searchBox;
    }
}