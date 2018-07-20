package gui;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class SearchFilterMenu extends VBox {

    private Text header;
    private ArrayList<FilterField> fields;
    private boolean buttonPress = false;
    private final Button selectAll;
    private final Button selectNone;

    public SearchFilterMenu(String subtitle, List<String> titles) {
        header = new Text(subtitle);
        header.setFill(Color.WHITE);
        fields = new ArrayList<>();
        titles.forEach(title -> fields.add(new FilterField(title)));
        //button action implementations are in SearchFilterBox.java due to need for access to fireUpdate() method
        selectAll = new Button("Select All");
        selectNone = new Button("Select None");
        HBox buttons = new HBox(selectAll, selectNone) {{setSpacing(5);}};

        getChildren().add(header);
        getChildren().addAll(fields);
        getChildren().add(buttons);

        setSpacing(10);
    }

    public Text getHeader() {
        return header;
    }

    public boolean isButtonPress() {
        return buttonPress;
    }

    public Button getSelectAll() {
        return selectAll;
    }

    public Button getSelectNone() {
        return selectNone;
    }

    public void setButtonPress(boolean buttonPress) {
        this.buttonPress = buttonPress;
    }

    class FilterField extends HBox {

        FilterField(String label) {
            this.checkBox = new CheckBox(){{setText(label); setTextFill(Color.WHITE); setSelected(true);}};
            getChildren().add(checkBox);
        }

        CheckBox checkBox;
        Text label;
    }

    public ArrayList<FilterField> getFields() {
        return fields;
    }
}
