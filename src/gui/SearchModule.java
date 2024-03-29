package gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Window;
import main.*;
import net.PostRequest;
import net.ThreadedCall;
import searchengine.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class SearchModule extends VBox {

    private final Main wrapper;
    private final SearchFilterBox filterBox;
    private SubtleTextField searchBox;
    private VBox textFillers;
    private ArrayList<String> textFillerStrings;
    private int fillersIndex = -1;
    private String lastActualTyped;
    private QueryEngine engine;
    private VBox searchResultsDisplay;
    private boolean expanded;
    private boolean lastChangeFromUser = true;
    private Text topBarSubtitle;
    private ScrollPane filters;
    private boolean lastSearchType; //false for stem, true for full search
    private String description = "What can I help you find?";

    SearchModule(QueryEngine engine, Main wrapper) {
        this.engine = engine;
        this.wrapper = wrapper;
        this.searchResultsDisplay = new VBox();
        searchResultsDisplay.setPadding(Size.insets(30, 0, 0, 0));
        textFillerStrings = new ArrayList<>();
        searchBox = new SubtleTextField("");
        Styles.setProperty(searchBox, "-fx-text-fill", Colors.colorToHex(Color.WHITE));
        searchBox.setEditable(true);
        searchBox.setPrefColumnCount(40);
        searchBox.setFont(Font.font("Sans Serif", Size.fontSize(30)));
        searchBox.setOnAction(event -> search());
        searchBox.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().equals(KeyCode.DOWN)) {
                if (textFillerStrings.size() == 0) {
                    event.consume();
                    return;
                }
                if (!expanded)
                    expand();
                if (!getChildren().contains(textFillers))
                    getChildren().add(1, textFillers);
                lastChangeFromUser = false;
                if (fillersIndex < textFillers.getChildren().size() - 1)
                    setFillerSelected(fillersIndex + 1);
                else setFillerSelected(-1);
                event.consume();
                return;
            }
            else if (event.getCode().equals(KeyCode.UP)) {
                if (textFillerStrings.size() == 0) {
                    event.consume();
                    return;
                }
                if (!expanded)
                    expand();
                lastChangeFromUser = false;
                if (fillersIndex >= 0)
                    setFillerSelected(fillersIndex - 1);
                else setFillerSelected(textFillers.getChildren().size() - 1);
                event.consume();
                return;
            }
            if (event.getCode().equals(KeyCode.TAB)) {
                if (textFillerStrings.size() > 0) {
                    if (lastChangeFromUser)
                        searchBox.setText(textFillerStrings.get(0) + " ");
                    else {
                        searchBox.setText(searchBox.getText() + " ");
                        search();
                    }
                    searchBox.positionCaret(searchBox.getText().length());
                }
                event.consume();
            }
            lastChangeFromUser = true;
        });
        searchBox.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
        });
        searchBox.textProperty().addListener((observable, oldText, newText) -> {
            if (lastChangeFromUser) {
                if (newText.length() > 0 && newText.trim().length() == 0) {
                    searchBox.setText("");
                    newText = "";
                }
                //TODO remove these two lines if we want to allow multiple consecutive spaces
                newText = newText.replaceAll("\\s+", " ");
                searchBox.setText(newText);
                // --------------------------------------------------------------------------
                lastActualTyped = newText;
                setFillerSelected(-1);
                if (newText.trim().length() == 0) {
                    fillersIndex = -1;
                    textFillerStrings.clear();
                    textFillers.getChildren().clear();
                    collapse();
                } else searchStem();
            }
        });
        textFillers = new VBox();
        Styles.setBackgroundColor(textFillers, Color.BLACK);
        textFillers.setBorder(Styles.defaultBorder(Color.WHITE));
        textFillers.setPadding(Size.insets(10));
        topBarSubtitle = wrapper.getSubtitle();
        getChildren().add(searchBox);
        Styles.setBackgroundColor(this, Color.BLACK);
        setPrefSize(Size.width(1920), Size.height(100));
        filterBox = new SearchFilterBox(this);
        filters = new ScrollPane(filterBox);
        filters.setStyle("-fx-background: transparent; -fx-background-color: transparent");
        filters.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        filters.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    private void setFillerSelected(int i) {
        if (fillersIndex >= 0)
            Events.fireMouse(textFillers.getChildren().get(fillersIndex), MouseEvent.MOUSE_EXITED);
        if (i < 0) searchBox.setText(lastActualTyped);
        else {
            Events.fireMouse(textFillers.getChildren().get(i), MouseEvent.MOUSE_ENTERED);
            if (fillersIndex < 0)
                lastActualTyped = searchBox.getText();

            searchBox.setText(textFillerStrings.get(i));
        }
        fillersIndex = i;
        searchBox.positionCaret(searchBox.getText() == null ? 0 : searchBox.getText().length());
    }

    void searchStem() {
        if (!expanded)
            expand();
        searchResultsDisplay.getChildren().clear();

        //adjust search suggestions
        textFillers.getChildren().clear();
        textFillerStrings.clear();

        String orig = searchBox.getText();
        int partition = searchBox.getText().lastIndexOf(" ") + 1;

        List<String> wordStems = engine.getWeightedPredictor().predict(orig, 10);
        String beginning = orig.substring(0, partition);
        for (String s : wordStems) {
            String full = beginning + s;
            textFillerStrings.add(full);
            HBox box = new HBox();
            Styles.setBackgroundColor(box, Color.BLACK);
            final AutoColoredLabel original = new AutoColoredLabel(full.substring(0, orig.length()), box);
            original.setFont(Font.font("Sans Serif", FontWeight.BOLD, Size.fontSize(16)));
            final AutoColoredLabel remaining = new AutoColoredLabel(full.substring(orig.length()), box);
            remaining.setFont(Font.font("Sans Serif", Size.fontSize(16)));
            box.getChildren().addAll(original, remaining);
            Events.highlightOnMouseOver(box);
            box.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
                if (textFillers.getChildren().indexOf(box) == fillersIndex && lastChangeFromUser) {
                    String newColorStr = Colors.colorToHex(Colors.highlightColor(Color.WHITE));
                    String newStyle = box.getStyle().replaceAll("-fx-background-color: #......", "-fx-background-color: " + newColorStr);
                    box.setStyle(newStyle);
                    event.consume(); //prevent highlightOnMouseOver() from triggering after this event
                }
            });
            box.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                searchBox.setText(full);
                search();
            });
            textFillers.getChildren().add(box);
        }
        Task<TreeSet<Identifier>> searchAction = new Task<>() {
            @Override
            protected TreeSet<Identifier> call() {
                return getEngine().incompleteQuery(beginning, orig.substring(partition), textFillerStrings, new FilterSet((SearchFilterBox) filters.getContent()));
            }
        };
        Thread searchThread = new Thread(searchAction);
        searchThread.setDaemon(true);
        searchThread.start();
        searchAction.setOnSucceeded(event -> {
            TreeSet<Identifier> result = searchAction.getValue();
            for (Identifier id : result) {
                searchResultsDisplay.getChildren().add(new ResultBlock(id));
            }
            setSubHeaderText();
        });
        if (!getChildren().contains(textFillers))
            getChildren().add(1, textFillers);
        fillersIndex = -1;
        lastSearchType = false;
    }

    private void setSubHeaderText() {
        int size = searchResultsDisplay.getChildren().size();
        if (size == 0)
            description = "No results were returned.";
        else description = "Returned " + size + (size == 1 ? " result" : " results") + " in " + UtilAndConstants.parseTimeNanos(getEngine().getLastQueryTimeNanos());
        topBarSubtitle.setText(description);
    }

    private void netRequest(String token) {
        ThreadedCall<List<ItemNode>> call = new ThreadedCall<>("search/query.php", true, new PostRequest("token", token));
        //TODO implment relevance algorithm for server results
        call.procedureCall((list) -> list.stream().map(ItemNode::fromRemoteRegex).collect(Collectors.toList()), (results) -> {
            //TODO
        });
    }

    void search() {
        if (!expanded)
            expand();
        getChildren().remove(textFillers);
        searchResultsDisplay.getChildren().clear();
        Task<List<Identifier>> searchAction = new Task<>() {
            @Override
            protected List<Identifier> call() {
                return getEngine().query(searchBox.getText(), new FilterSet((SearchFilterBox) filters.getContent()));
            }
        };
        Thread searchThread = new Thread(searchAction);
        searchThread.setDaemon(true);
        searchThread.start();
        searchAction.setOnSucceeded(event -> {
            List<Identifier> result = searchAction.getValue();
            for (Identifier id : result) {
                searchResultsDisplay.getChildren().add(new ResultBlock(id));
            }
            if (result.size() == 0 && !getFilterBox().isDefault())
                collapse();
            setSubHeaderText();
        });
        lastSearchType = true;
    }

    public QueryEngine getEngine() {
        return engine;
    }

    public void setEngine(QueryEngine engine) {
        this.engine = engine;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isLastSearchType() {
        return lastSearchType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SearchFilterBox getFilterBox() {
        return filterBox;
    }

    class ResultBlock extends HBox {

        private Identifier id;
        private boolean expanded = false;

        ResultBlock(Identifier id) {
            this.id = id;
            double size = Size.fontSize(15);
            setStyle("-fx-background-color: #000000");
            Events.highlightOnMouseOver(this);
            Text title = new Text(id.getName());
            title.setFill(Color.WHITE);
            title.setFont(Font.font(size * 3/2));
            Text subtitle = new Text();
            subtitle.setFill(Color.WHITE);
            subtitle.setFont(Font.font(subtitle.getFont().getFamily(), FontPosture.ITALIC, size));
            VBox titles = new VBox(title, subtitle);
            getChildren().add(titles);
            Text date1 = makeDate(id.getTime1());
            VBox dates = new VBox() {{setAlignment(Pos.CENTER_RIGHT);}}; //can hold other information besides dates if need be (see switch block below)

            switch (id.getType()) {
                case Post: {
                    subtitle.setText("Post in " + id.getBelongsTo().toString());
                    dates.getChildren().add(date1);
                    break;
                }
                case Class: subtitle.setText(id.getBelongsTo().getTeacherFirst() + " " + id.getBelongsTo().getTeacherLast() + " Period " + id.getBelongsTo().getPeriodNo());
                date1.setText("Not in session");
                    dates.getChildren().add(date1);
                    break;
                case Class_Item: subtitle.setText(id.getBelongsTo().toString());
                    dates.getChildren().add(date1);
                    if (id.getTime2() > 0) {
                        Text date2 = makeDate(id.getTime2());
                        dates.getChildren().add(date2);
                    }
                    break;
                case Organization: subtitle.setText("Organization");
                    break;
                case Module: subtitle.setText("Lesson by " + id.getAuthorName());
                    date1.setText("Uploaded " + date1.getText());
                    dates.getChildren().add(date1);
                    Text views = new Text(UtilAndConstants.parseLargeNumber(id.getViews()) + " views");
                    dates.getChildren().add(views);
                    Text likes = makeDate(id.getTime2()); //formatting only
                    likes.setText(UtilAndConstants.parseLargeNumber(id.getLikes()) + "likes");
                    dates.getChildren().add(likes);
                    break;
                case Setting: subtitle.setText("Account Settings");
                    break;
                case Utility: break;
            }
            getChildren().add(new Layouts.Filler());
            getChildren().add(dates);

            addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getButton().equals(MouseButton.SECONDARY)) {
                    if (expanded) collapse();
                    else expand();
                } else {
                    //search feedback
                    String query = searchBox.getText();
                    int lastSpace = query.lastIndexOf("\\s+");
                    String lastWord = query.substring(lastSpace + 1);
                    String validWords = lastSpace == -1 ? "" : query.substring(0, lastSpace);
                    WeightedPredictor predictor = SearchModule.this.getEngine().getWeightedPredictor();
                    if (predictor.getRoot().getStemIndex().contains(lastWord))
                        validWords += " " + lastWord;
                    if (validWords.trim().length() > 0)
                        SearchModule.this.getEngine().getWeightedPredictor().associate(validWords.toLowerCase().trim());

                    //find object represented by identifier and trigger launch event
                    Indexable obj = id.find(getEngine().getIndex());
                    SearchModule.this.collapse();
                    if (obj == null)
                        throw new IllegalStateException("Identifier pointing to null Indexable object - id: " + id.getId());
                    wrapper.closeSearchBar();
                    obj.launch();
                }
            });

            setPadding(Size.insets(10));

        }

        void expand() {
            getChildren().set(1, id.find(engine.getIndex()).getDetailText(textFillerStrings));
            expanded = true;
        }

        void collapse() {
            getChildren().set(1, new Layouts.Filler());
            expanded = false;
        }

        Text makeDate(long time) {
            return new Text(UtilAndConstants.parseTimestamp(new Timestamp(time))) {{setFont(Font.font(Size.fontSize(20))); setFill(Color.WHITE);}};
        }
    }

    private void expand() {
        wrapper.expandTopBar();
        if (!getChildren().contains(searchResultsDisplay))
            getChildren().add(searchResultsDisplay);
        if (!wrapper.getTitles().getChildren().contains(filters)) {
            wrapper.getTitles().getChildren().add(filters);
        }
        expanded = true;
    }

    void collapse() {
        getChildren().remove(textFillers);
        getChildren().remove(searchResultsDisplay);
        topBarSubtitle.setText("What can I help you find?");
        wrapper.getTitles().getChildren().remove(filters);
        if (expanded)
            wrapper.collapseTopBar();
        expanded = false;
    }

    public TextField getSearchBox() {
        return searchBox;
    }



}
