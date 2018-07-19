package gui;

import classes.ClassPd;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import main.UtilAndConstants;
import searchengine.Identifier;
import searchengine.Indexable;
import searchengine.QueryEngine;
import searchengine.WeightedPredictor;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class SearchModule extends VBox {

    private final Main wrapper;
    private TextField searchBox;
    private VBox textFillers;
    private ArrayList<String> textFillerStrings;
    private int fillersIndex = -1;
    private String lastActualTyped;
    private QueryEngine engine;
    private VBox searchResultsDisplay;
    private boolean expanded;
    private boolean lastChangeFromUser = true;
    private Text topBarSubtitle;

    public SearchModule(QueryEngine engine, Main wrapper) {
        this.engine = engine;
        this.wrapper = wrapper;
        this.searchResultsDisplay = new VBox();
        textFillerStrings = new ArrayList<>();
        searchBox = new TextField("");
        searchBox.setEditable(true);
        searchBox.setPrefColumnCount(40);
        searchBox.setFont(Font.font("Sans Serif", 30));
        searchBox.setOnAction(event -> search());
        searchBox.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().equals(KeyCode.DOWN)) {
                lastChangeFromUser = false;
                if (fillersIndex < textFillers.getChildren().size() - 1)
                    setFillerSelected(fillersIndex + 1);
                else setFillerSelected(-1);
            }
            else if (event.getCode().equals(KeyCode.UP)) {
                lastChangeFromUser = false;
                if (fillersIndex >= 0)
                    setFillerSelected(fillersIndex - 1);
                else setFillerSelected(textFillers.getChildren().size() - 1);
                event.consume();
            }
            lastChangeFromUser = true;
        });
        searchBox.textProperty().addListener((observable, oldText, newText) -> {
            if (lastChangeFromUser) {
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
        textFillers.setStyle("-fx-background-color: white");
        textFillers.setPadding(new Insets(10));
        topBarSubtitle = wrapper.getSubtitle();
        getChildren().add(searchBox);
        setStyle("-fx-background-color: black");
        setPrefSize(1920, 100);
        getEngine().getIndex().associate("search", new Identifier("Test ID", Identifier.Type.Post, 1) {{setTime1(System.currentTimeMillis()); setBelongsTo(new ClassPd());}}, 1);
    }

    private void setFillerSelected(int i) {
        if (fillersIndex >= 0)
            UtilAndConstants.fireMouse(textFillers.getChildren().get(fillersIndex), MouseEvent.MOUSE_EXITED);
        if (i < 0) searchBox.setText(lastActualTyped);
        else {
            UtilAndConstants.fireMouse(textFillers.getChildren().get(i), MouseEvent.MOUSE_ENTERED);
            if (fillersIndex < 0)
                lastActualTyped = searchBox.getText();

            searchBox.setText(textFillerStrings.get(i));
        }
        fillersIndex = i;
        searchBox.positionCaret(searchBox.getText() == null ? 0 : searchBox.getText().length());
    }

    private void searchStem() {
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
            HBox box = new HBox(new Text(full.substring(0, orig.length())){{setFont(Font.font("Sans Serif", FontWeight.BOLD, 16));}}, new Text(full.substring(orig.length())) {{setFont(Font.font("Sans Serif", 16));}});
            box.setStyle("-fx-background-color: #ffffff");
            UtilAndConstants.highlightOnMouseOver(box);
            box.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
                if (textFillers.getChildren().indexOf(box) == fillersIndex && lastChangeFromUser) {
                    String newColorStr = UtilAndConstants.colorToHex(UtilAndConstants.highlightColor(Color.WHITE));
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
        TreeSet<Identifier> result = getEngine().incompleteQuery(textFillerStrings);
        for (Identifier id : result) {
            searchResultsDisplay.getChildren().add(new ResultBlock(id));
        }
        if (!getChildren().contains(textFillers))
            getChildren().add(1, textFillers);
        setSubHeaderText();
        fillersIndex = -1;
    }

    private void setSubHeaderText() {
        int size = searchResultsDisplay.getChildren().size();
        if (size == 0)
            topBarSubtitle.setText("No results were returned.");
        else topBarSubtitle.setText("Returned " + size + (size == 1 ? " result" : " results") + " in " + UtilAndConstants.parseTimeNanos(getEngine().getLastQueryTimeNanos()));
    }

    private void search() {
        if (!expanded)
            expand();
        fillersIndex = -1;
        getChildren().remove(textFillers);
        searchResultsDisplay.getChildren().clear();
        List<Identifier> result = getEngine().query(searchBox.getText());
        for (Identifier id : result) {
            searchResultsDisplay.getChildren().add(new ResultBlock(id));
        }
        if (result.size() == 0)
            collapse();
        setSubHeaderText();
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

    class ResultBlock extends HBox {

        private Identifier id;

        ResultBlock(Identifier id) {
            this.id = id;
            setStyle("-fx-background-color: #202020");
            UtilAndConstants.highlightOnMouseOver(this);
            Text title = new Text(id.getName());
            title.setFill(Color.WHITE);
            Text subtitle = new Text();
            subtitle.setFill(Color.WHITE);
            subtitle.setFont(Font.font(subtitle.getFont().getFamily(), FontPosture.ITALIC, subtitle.getFont().getSize()));
            VBox titles = new VBox(title, subtitle);
            getChildren().add(titles);
            Text date1 = makeDate(id.getTime1());
            VBox dates = new VBox(); //can hold other information besides dates if need be (see switch block below)

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
                    Text views = new Text(parseLargeNumber(id.getViews()) + " views");
                    dates.getChildren().add(views);
                    Text likes = makeDate(id.getTime2()); //formatting only
                    likes.setText(parseLargeNumber(id.getLikes()) + "likes");
                    dates.getChildren().add(likes);
                    break;
                case Setting: subtitle.setText("Account Settings");
                    break;
                case Utility: break;
            }

            addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
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
                obj.launch();
            });

        }

        void expand() {
            //TODO show detail text
        }

        void collapse() {
            //TODO
        }

        Text makeDate(long time) {
            return new Text(UtilAndConstants.parseTimestamp(new Timestamp(time)));
        }
    }

    private static String parseLargeNumber(long num) {
        if (num < 1E4) return Long.toString(num);
        if (num < 1E6) return num / 1000 + "K";
        if (num < 1E9) return new DecimalFormat("%.1f").format((double) num / 1000000.0) + "M";
        return new DecimalFormat("%.1f").format((double) num / 1000000000.0) + "B";
    }

    void expand() {
        wrapper.expandTopBar();
        if (!getChildren().contains(searchResultsDisplay))
            getChildren().add(searchResultsDisplay);
        expanded = true;
    }

    void collapse() {
        getChildren().remove(textFillers);
        getChildren().remove(searchResultsDisplay);
        topBarSubtitle.setText("What can I help you find?");
        if (expanded)
            wrapper.collapseTopBar();
        expanded = false;
    }

    public TextField getSearchBox() {
        return searchBox;
    }

    String frontTrim(String s) {
        int n = 0;
        while (s.charAt(n) == ' ')
            n++;
        return s.substring(n);
    }
}
