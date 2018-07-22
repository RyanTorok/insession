package gui;

import classes.ClassPd;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import main.Root;
import searchengine.FilterSet;
import searchengine.Identifier;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
    private SearchModule wrapper;

    public SearchFilterBox(SearchModule wrapper) {
        this.wrapper = wrapper;
        header = new Text("Filter Results") {{setFill(Color.WHITE); setFont(Font.font("Sans Serif", FontWeight.BOLD, Root.fontSize(20)));}};
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
        indexedClasses.addAll(Root.getActiveUser().getClassesTeacher());
        indexedClasses.addAll(Root.getActiveUser().getClassesStudent());
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
        dateConstraintGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> fireUpdate());
        RadioButton on = new RadioButton("On"), after = new RadioButton("After"), before = new RadioButton("Before"), today = new RadioButton("Today"), thisWeek = new RadioButton("Past Week");

        on.setTextFill(Color.WHITE);
        on.setToggleGroup(dateConstraintGroup);
        on.setOnAction(event -> datePicker.setValue(datePickerLastDate));

        after.setTextFill(Color.WHITE);
        after.setToggleGroup(dateConstraintGroup);
        after.setOnAction(event -> datePicker.setValue(datePickerLastDate));

        before.setTextFill(Color.WHITE);
        before.setToggleGroup(dateConstraintGroup);
        before.setOnAction(event -> datePicker.setValue(datePickerLastDate));

        today.setTextFill(Color.WHITE);
        today.setToggleGroup(dateConstraintGroup);
        today.setOnAction(event -> {
            if (datePicker.getValue() != null)
                datePickerLastDate = datePicker.getValue();
            datePicker.setValue(null);
        });

        thisWeek.setTextFill(Color.WHITE);
        thisWeek.setToggleGroup(dateConstraintGroup);
        thisWeek.setOnAction(event -> {
            if (datePicker.getValue() != null)
                datePickerLastDate = datePicker.getValue();
            datePicker.setValue(null);
        });

        datePicker = new DatePicker();
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            fireUpdate();
        });

        Text dateHeader = new Text("By Date") {{setFill(Color.WHITE); setFont(filterSets.get(0).getHeader().getFont());}};
        Button resetDateFilter = new Button("Reset") {{setOnAction(event -> {dateConstraintGroup.selectToggle(null); datePicker.setValue(null);});}};

        dateFilter = new VBox(dateHeader, today, thisWeek, on, after, before, datePicker, resetDateFilter) {{setSpacing(filterSets.get(0).getSpacing());}};

        getChildren().add(header);
        getChildren().addAll(filterSets);
        getChildren().add(dateFilter);



        setSpacing(Root.height(20));
        setPadding(Root.insets(20));
    }

    private void fireUpdate() {
        if (wrapper.isLastSearchType())
            wrapper.search();
        else wrapper.searchStem();
    }

    public boolean isSelected(Identifier.Type type) {
        switch (type) {
            case Class_Item:   return filterSets.get(0).getFields().get(0).checkBox.isSelected();
            case Module:       return filterSets.get(0).getFields().get(1).checkBox.isSelected();
            case Post:         return filterSets.get(0).getFields().get(2).checkBox.isSelected();
            case Class:        return filterSets.get(0).getFields().get(3).checkBox.isSelected();
            case Organization: return filterSets.get(0).getFields().get(4).checkBox.isSelected();
            case Utility:      return filterSets.get(0).getFields().get(5).checkBox.isSelected();
            case Setting:      return filterSets.get(0).getFields().get(6).checkBox.isSelected();
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
        LocalDate localDate = datePicker.getValue();
        if (localDate == null)
            return new Date(System.currentTimeMillis());
        Instant instant = Instant.from(localDate.atStartOfDay(ZoneId.systemDefault()));
        return Date.from(instant);
    }

    public FilterSet.DateConstraint getDateConstraint() {
        int i = 0;
        for (Toggle toggle : dateConstraintGroup.getToggles()) {
            if (toggle.isSelected())
                return FilterSet.DateConstraint.values()[i];
            i++;
        }
        return FilterSet.DateConstraint.NONE;
    }

    public ArrayList<ClassPd> getIndexedClasses() {
        return indexedClasses;
    }
}
