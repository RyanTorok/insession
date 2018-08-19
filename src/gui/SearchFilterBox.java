package gui;

import classes.ClassPd;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import main.Size;
import main.User;
import main.UtilAndConstants;
import searchengine.FilterSet;
import searchengine.Identifier;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;

public class SearchFilterBox extends VBox {
    private Text header;
    private ArrayList<SearchFilterMenu> filterSets;
    private ArrayList<ClassPd> indexedClasses;
    private DatePicker datePicker;
    private LocalDate datePickerLastDate;
    private final ToggleGroup dateConstraintGroup;
    private final VBox dateFilter;
    private FilterSet.DateConstraint selectedDateConstraint = FilterSet.DateConstraint.NONE;
    private SearchModule wrapper;

    public SearchFilterBox(SearchModule wrapper) {
        this.wrapper = wrapper;
        header = new Text("Filter Results") {{
            setFill(Color.WHITE);
            setFont(Font.font("Sans Serif", FontWeight.BOLD, Size.fontSize(20)));
        }};
        filterSets = new ArrayList<>();
        ArrayList<String> types = new ArrayList<>();
        types.add("Files and Assignments");
        types.add("Modules");
        types.add("Posts");
        types.add("Classes");
        types.add("Organizations");
        types.add("Utilities");
        types.add("Settings");
        filterSets.add(new SearchFilterMenu("By Type", types));
        indexedClasses = new ArrayList<>();
        indexedClasses.addAll(User.active().getClassesTeacher());
        indexedClasses.addAll(User.active().getClassesStudent());
        filterSets.add(new SearchFilterMenu("By Class", indexedClasses.stream().map(ClassPd::toString).collect(Collectors.toList())));
        filterSets.forEach(set -> {
            set.getFields().forEach(field -> field.checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (!set.isButtonPress()) {
                    fireUpdate();
                }
            }));
            set.getSelectAll().setOnAction(event -> {
                set.setButtonPress(true);
                set.getFields().forEach(filterField -> filterField.checkBox.setSelected(true));
                set.setButtonPress(false);
                fireUpdate();
            });
            set.getSelectNone().setOnAction(event -> {
                set.setButtonPress(true);
                set.getFields().forEach(filterField -> filterField.checkBox.setSelected(false));
                set.setButtonPress(false);
                fireUpdate();
            });
        });

        //date constraint filter
        dateConstraintGroup = new ToggleGroup();

        RadioButton today = new RadioButton("Today");
        RadioButton thisWeek = new RadioButton("Past Week");
        RadioButton on = new RadioButton("On");
        RadioButton after = new RadioButton("After");
        RadioButton before = new RadioButton("Before");

        today.setTextFill(Color.WHITE);
        today.setToggleGroup(dateConstraintGroup);
        today.setOnAction(event -> {
            datePicker.setValue(null);
            selectedDateConstraint = FilterSet.DateConstraint.TODAY;
            fireUpdate();
        });

        thisWeek.setTextFill(Color.WHITE);
        thisWeek.setToggleGroup(dateConstraintGroup);
        thisWeek.setOnAction(event -> {
            datePicker.setValue(null);
            selectedDateConstraint = FilterSet.DateConstraint.PAST_WEEK;
            fireUpdate();
        });

        on.setTextFill(Color.WHITE);
        on.setToggleGroup(dateConstraintGroup);
        on.setOnAction(event -> {
            datePicker.setValue(datePickerLastDate);
            selectedDateConstraint = FilterSet.DateConstraint.ON;
            fireUpdate();
        });

        after.setTextFill(Color.WHITE);
        after.setToggleGroup(dateConstraintGroup);
        after.setOnAction(event -> {
            datePicker.setValue(datePickerLastDate);
            selectedDateConstraint = FilterSet.DateConstraint.AFTER;
            fireUpdate();
        });

        before.setTextFill(Color.WHITE);
        before.setToggleGroup(dateConstraintGroup);
        before.setOnAction(event -> {
            datePicker.setValue(datePickerLastDate);
            selectedDateConstraint = FilterSet.DateConstraint.BEFORE;
            fireUpdate();
        });


        datePicker = new DatePicker();
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            fireUpdate();
            datePickerLastDate = datePicker.getValue();
        });

        Text dateHeader = new Text("By Date") {{
            setFill(Color.WHITE);
            setFont(filterSets.get(0).getHeader().getFont());
        }};
        Button resetDateFilter = new Button("Reset") {{
            setOnAction(event -> {
                dateConstraintGroup.selectToggle(null);
                datePicker.setValue(null);
                datePickerLastDate = null;
                selectedDateConstraint = FilterSet.DateConstraint.NONE;
            });
        }};

        dateFilter = new VBox(dateHeader, today, thisWeek, on, after, before, datePicker, resetDateFilter) {{
            setSpacing(filterSets.get(0).getSpacing());
        }};

        getChildren().add(header);
        getChildren().addAll(filterSets);
        getChildren().add(dateFilter);


        setSpacing(Size.height(20));
        setPadding(Size.insets(20));
    }

    private void fireUpdate() {
        if (wrapper.isLastSearchType())
            wrapper.search();
        else wrapper.searchStem();
    }

    public boolean isSelected(Identifier.Type type) {
        switch (type) {
            case Class_Item:
                return filterSets.get(0).getFields().get(0).checkBox.isSelected();
            case Module:
                return filterSets.get(0).getFields().get(1).checkBox.isSelected();
            case Post:
                return filterSets.get(0).getFields().get(2).checkBox.isSelected();
            case Class:
                return filterSets.get(0).getFields().get(3).checkBox.isSelected();
            case Organization:
                return filterSets.get(0).getFields().get(4).checkBox.isSelected();
            case Utility:
                return filterSets.get(0).getFields().get(5).checkBox.isSelected();
            case Setting:
                return filterSets.get(0).getFields().get(6).checkBox.isSelected();
        }
        return false;
    }

    public boolean isSelected(ClassPd classPd) {
        if (classPd == null)
            return false;
        int index = indexedClasses.indexOf(classPd);
        return filterSets.get(1).getFields().get(index).checkBox.isSelected();
    }

    public Date getDateRestriction() {
        return UtilAndConstants.date(datePicker.getValue());
    }

    public FilterSet.DateConstraint getDateConstraint() {
        return selectedDateConstraint;
    }

    public ArrayList<ClassPd> getIndexedClasses() {
        return indexedClasses;
    }

    //returns true if the filters are all set to their default options.
    // Used to allow or suppress the search module collapse when no results are returned.
    public boolean isDefault() {
        for (SearchFilterMenu menu :
                filterSets) {
            for (Node n :
                    menu.getChildren()) {
                if (n instanceof CheckBox) {
                    if (!((CheckBox) n).isSelected())
                        return false;
                } else if (n instanceof RadioButton) {
                    if (!((RadioButton) n).isSelected())
                        return false;
                }
            }
        }
        for (Node n :
                dateFilter.getChildren()) {
            if (n instanceof RadioButton)
                if (!((RadioButton) n).isSelected())
                    return false;
        }
        return true;
    }
}
