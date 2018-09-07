package gui;

import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import searchengine.FilterSet;
import searchengine.Identifier;
import searchengine.Indexable;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Consumer;

public class DateFilter {

    private final ToggleGroup dateConstraintGroup;
    private FilterSet.DateConstraint selectedDateConstraint;
    private LocalDate datePickerLastDate;
    private VBox main;
    private final Text dateHeader;
    private final DatePicker datePicker;


    public DateFilter(Consumer event) {
        this(event, Color.WHITE);
    }

    public DateFilter(Consumer event, Color textFill) {
        //date constraint filter
        dateConstraintGroup = new ToggleGroup();

        RadioButton today = new RadioButton("Today");
        RadioButton thisWeek = new RadioButton("Past Week");
        RadioButton on = new RadioButton("On");
        RadioButton after = new RadioButton("After");
        RadioButton before = new RadioButton("Before");
        datePicker = new DatePicker();
        today.setTextFill(textFill);
        today.setToggleGroup(dateConstraintGroup);
        selectedDateConstraint = null;
        today.setOnAction(event1 -> {
            datePicker.setValue(null);
            selectedDateConstraint = FilterSet.DateConstraint.TODAY;
            event.accept(null);
        });

        thisWeek.setTextFill(textFill);
        thisWeek.setToggleGroup(dateConstraintGroup);
        thisWeek.setOnAction(event1 -> {
            datePicker.setValue(null);
            selectedDateConstraint = FilterSet.DateConstraint.PAST_WEEK;
            event.accept(null);
        });

        on.setTextFill(textFill);
        on.setToggleGroup(dateConstraintGroup);
        on.setOnAction(event1 -> {
            datePicker.setValue(datePickerLastDate);
            selectedDateConstraint = FilterSet.DateConstraint.ON;
            event.accept(null);
        });

        after.setTextFill(textFill);
        after.setToggleGroup(dateConstraintGroup);
        after.setOnAction(event1 -> {
            datePicker.setValue(datePickerLastDate);
            selectedDateConstraint = FilterSet.DateConstraint.AFTER;
            event.accept(null);
        });

        before.setTextFill(textFill);
        before.setToggleGroup(dateConstraintGroup);
        before.setOnAction(event1 -> {
            datePicker.setValue(datePickerLastDate);
            selectedDateConstraint = FilterSet.DateConstraint.BEFORE;
            event.accept(null);
        });


        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            event.accept(null);
            datePickerLastDate = datePicker.getValue();
        });

        dateHeader = new Text("By Date") {{
            setFill(textFill);
        }};
        Button resetDateFilter = new Button("Reset") {{
            setOnAction(event -> {
                dateConstraintGroup.selectToggle(null);
                datePicker.setValue(null);
                datePickerLastDate = null;
                selectedDateConstraint = FilterSet.DateConstraint.NONE;
            });
        }};

        selectedDateConstraint = FilterSet.DateConstraint.NONE;

        main = new VBox(dateHeader, today, thisWeek, on, after, before, datePicker, resetDateFilter);
    }

    public VBox get() {
        return main;
    }

    public ToggleGroup getDateConstraintGroup() {
        return dateConstraintGroup;
    }

    public FilterSet.DateConstraint getSelectedDateConstraint() {
        return selectedDateConstraint;
    }

    public void setSelectedDateConstraint(FilterSet.DateConstraint selectedDateConstraint) {
        this.selectedDateConstraint = selectedDateConstraint;
    }

    public LocalDate getDatePickerLastDate() {
        return datePickerLastDate;
    }

    public void setDatePickerLastDate(LocalDate datePickerLastDate) {
        this.datePickerLastDate = datePickerLastDate;
    }

    public VBox getMain() {
        return main;
    }

    public void setMain(VBox main) {
        this.main = main;
    }

    public Text getDateHeader() {
        return dateHeader;
    }

    public DatePicker getDatePicker() {
        return datePicker;
    }

    public boolean matches(Indexable item) {
        Identifier identifier = item.getUniqueIdentifier();
        long time1 = identifier.getTime1(), time2 = identifier.getTime2();
        int ms = 86400000; //milliseconds in a day
        LocalDate dval = datePicker.getValue();
        if (dval == null)
            dval = LocalDate.now();
        Date pickerVal = Date.from(dval.atStartOfDay(ZoneId.systemDefault()).toInstant());
        switch (selectedDateConstraint) {
            case TODAY: {
                long now = System.currentTimeMillis();
                long today = now - now % ms;
                long diff1 = time1 - today;
                long diff2 = time2 - today;
                return (diff1 > 0 && diff1 < ms) || (diff2 > 0 && diff2 < ms);
            }
            case PAST_WEEK: {
                Date idDate = new Date(time1 - time1 % ms);
                long now = System.currentTimeMillis();
                Date lastWeek = new Date(Math.max(0, now - (now % ms + ms * 7)));
                return idDate.after(lastWeek) || new Date(time2 - time2 % ms).after(lastWeek);
            }
            case ON:
                long diff1 = time1 - pickerVal.getTime();
                long diff2 = time2 - pickerVal.getTime();
                return (diff1 >= 0 && diff1 < ms) || (diff2 >= 0 && diff2 < ms);
            case AFTER:
                return new Date(time1).after(pickerVal) || new Date(time2).after(pickerVal);
            case BEFORE: {
                return (new Date(time1).before(pickerVal) && (time1 != 0)) || (new Date(time2).before(pickerVal) && (time2 != 0));
            }
            case NONE:
                return true;
        }
        return true;
    }
}
