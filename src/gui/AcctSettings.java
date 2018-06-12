package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.PasswordManager;
import main.Root;
import main.UtilAndConstants;

public class AcctSettings extends Stage {

    private Scene root = null;

    public AcctSettings() {
        super();
        setTitle("Account Settings");
        setMaximized(false);
        setResizable(false);
        initModality(Modality.APPLICATION_MODAL);
        GridPane layout = new GridPane();

        //change password
        Text cpPrompt = new Text("Change Password");
        Text opPrompt = new Text("Old Password  ");
        PasswordField oldPassword = new PasswordField();
        oldPassword.setPrefColumnCount(20);
        oldPassword.setEditable(true);
        HBox oldPasswordWrapper = new HBox(opPrompt, new Filler(), oldPassword);
        oldPasswordWrapper.setAlignment(Pos.CENTER);

        Text npPrompt = new Text("New Password  ");
        PasswordField newPassword = new PasswordField();
        newPassword.setPrefColumnCount(20);
        newPassword.setEditable(true);
        HBox newPasswordWrapper = new HBox(npPrompt, new Filler(), newPassword);
        newPasswordWrapper.setAlignment(Pos.CENTER);

        Text cfpPrompt = new Text("Confirm New Password  ");
        PasswordField cfPassword = new PasswordField();
        cfPassword.setPrefColumnCount(20);
        cfPassword.setEditable(true);
        HBox cfPasswordWrapper = new HBox(cfpPrompt, new Filler(), cfPassword);
        cfPasswordWrapper.setAlignment(Pos.CENTER);

        Text invalidMsg = new Text("");

        Button submit = new Button("Submit");
        submit.setOnAction(event -> {
            if (!PasswordManager.attempt(oldPassword.getText(), Root.getActiveUser())) {
                invalidMsg.setText("Your old password is incorrect.");
                return;
            }
            if (!cfPassword.getText().equals(newPassword.getText())) {
                invalidMsg.setText("Your passwords do not match. Check your spelling.");
                return;
            }
            if (oldPassword.getText().equals(newPassword.getText())) {
                invalidMsg.setText("Your old and new passwords may not be the same.");
                return;
            }
            String error = PasswordManager.validate(newPassword.getText());
            invalidMsg.setText(error);
            if (error.length() == 0) {
                boolean success = Root.getActiveUser().setPassword(newPassword.getText());
                if (!success) {
                    invalidMsg.setText("A connection error occurred. Please try again.");
                }
            }
        });

        VBox changePassword = new VBox(cpPrompt, oldPasswordWrapper, newPasswordWrapper, cfPasswordWrapper, invalidMsg, submit);
        changePassword.setPadding(new Insets(10));
        changePassword.setAlignment(Pos.CENTER);
        changePassword.setSpacing(10);

        layout.add(changePassword, 0, 0, 2, 1);

        //accent color
        Text accentColorPrompt = new Text("Accent Color: ");
        Shape colorSquare = new Rectangle(50, 50);
        Color original = Root.getActiveUser().getAccentColor();
        colorSquare.setFill(original);
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(Root.getActiveUser().getAccentColor());
        colorPicker.setOnAction(event1 -> {
            colorSquare.setFill(colorPicker.getValue());
            Root.getActiveUser().setAccentColor(colorPicker.getValue());
            Root.getPortal().getTop_bar().setStyle(Root.getPortal().getTop_bar().getStyle().replaceAll("-fx-border-color: #......", "-fx-border-color: " + UtilAndConstants.colorToHex(colorPicker.getValue())));
            Root.getPortal().getSideBar().setColor(colorPicker.getValue());
            if (!colorPicker.getValue().equals(original)) {
                Root.getPortal().getSubtitle().setText("Feeling a different color today?");
            }
        });
        VBox accentColor = new VBox(accentColorPrompt, colorSquare, colorPicker);
        accentColor.setSpacing(10);
        accentColor.setPadding(new Insets(10));
        layout.add(accentColor, 0, 1);

        //clock format
        Text cfPrompt = new Text("Clock Format");
        ToggleGroup cfGroup = new ToggleGroup();
        RadioButton twelve = new RadioButton("12 Hour");
        RadioButton twentyFour = new RadioButton("24 Hour");
        twelve.setToggleGroup(cfGroup);
        twentyFour.setToggleGroup(cfGroup);
        cfGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> Root.getActiveUser().setClock24Hour(newValue == twentyFour));
        if (Root.getActiveUser().isClock24Hour())
            twentyFour.setSelected(true);
        else twelve.setSelected(true);

        VBox clockFormat = new VBox(cfPrompt, twelve, twentyFour);
        clockFormat.setSpacing(10);
        clockFormat.setPadding(new Insets(10));
        layout.add(clockFormat, 0, 2);

        //Zip Code for Weather
        Text zcPrompt = new Text("Zip Code for Weather");
        TextField zcField = new TextField(String.format("%05d", Root.getActiveUser().getZipcode()));
        zcField.setPrefColumnCount(5);
        zcField.setEditable(true);
        zcField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                zcField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        Text zcInvalidMsg = new Text("");
        Button zcSave = new Button("Save");
        zcSave.setOnAction(event -> {
            zcInvalidMsg.setText("");
            if (zcField.getText().length() == 5) {
                Root.getActiveUser().setLocation(Integer.parseInt(zcField.getText()));
                Root.getPortal().updateWeather();
            } else {
                zcInvalidMsg.setText("Invalid Zip Code");
            }
        });
        VBox zipcode = new VBox(zcPrompt, zcField, zcInvalidMsg, zcSave);
        zipcode.setSpacing(10);
        zipcode.setPadding(new Insets(10, 10, 10, 60));
        layout.add(zipcode, 1, 1);

        //temperature units
        Text tuPrompt = new Text("Temperature Units");
        ToggleGroup tuGroup = new ToggleGroup();
        RadioButton fahrenheit = new RadioButton("Fahrenheit");
        RadioButton celsius = new RadioButton("Celsius");
        fahrenheit.setToggleGroup(tuGroup);
        celsius.setToggleGroup(tuGroup);
        tuGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            Root.getActiveUser().setTempUnits(newValue == fahrenheit);
            Root.getPortal().updateWeather();
        });
        if (Root.getActiveUser().usesFahrenheit())
            fahrenheit.setSelected(true);
        else celsius.setSelected(true);

        VBox temperatureUnits = new VBox(tuPrompt, fahrenheit, celsius);
        temperatureUnits.setSpacing(10);
        temperatureUnits.setPadding(new Insets(10, 10, 10, 60));
        layout.add(temperatureUnits, 1, 2);

        //close button
        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(event -> this.close());
        VBox rootpane = new VBox(layout, closeBtn);
        rootpane.setAlignment(Pos.TOP_CENTER);
        rootpane.setPadding(new Insets(20));
        root = new Scene(rootpane);
        setScene(root);
    }
    private class Filler extends Region {
        public Filler() {
            HBox.setHgrow(this, Priority.ALWAYS);
        }
    }
}
