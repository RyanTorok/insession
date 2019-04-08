package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.*;
import net.ServerSession;
import terminal.Address;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class AccountSettings extends Stage {

    private Scene root = null;

    public AccountSettings() {
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
                //confirm old password
//                Login login = new Login(User.active().getUsername(), oldPassword.getText(), false);
                String username = User.active().getUsername();
                try {
                    ServerSession session = new ServerSession();
                    String oldPasswordText = oldPassword.getText();
                    boolean opened = session.open(username, oldPasswordText);
                    if (!opened) {
                        if (session.getTruncatedErrorMsg().equalsIgnoreCase("authentication failure"))
                            invalidMsg.setText("Your old password is incorrect.");
                        else
                            invalidMsg.setText("A connection error occurred. Please try again.");
                    }
                    String password = newPassword.getText();
                    PasswordManager.PasswordCombo encryptedPassword = PasswordManager.newGenLocal(password, username);
                    byte[] pwd = encryptedPassword.getEncryptedPassword();
                    String encodedPwd = Base64.getEncoder().encodeToString(pwd).replaceAll("\\+", "!");
                    boolean success = session.sendOnly("changepassword", encodedPwd);
                    session.close();
                    if (success) {
                        invalidMsg.setText("Your password has been successfully changed.");
                        oldPassword.setText("");
                        newPassword.setText("");
                        cfPassword.setText("");
                        User.active().setPassword(password.getBytes(StandardCharsets.UTF_8));
                        User.active().setPasswordSalt(encryptedPassword.getSalt());
                    } else {
                        invalidMsg.setText("A connection error occurred. Please try again.");
                    }
                } catch (IOException e) {
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
        Text accentColorPrompt = new Text("Accent Color");
        Shape colorSquare = new Rectangle(50, 50);
        Color original = User.active().getAccentColor();
        colorSquare.setFill(original);
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(User.active().getAccentColor());
        colorPicker.setOnAction(event1 -> {
            colorSquare.setFill(colorPicker.getValue());
            User.active().setAccentColor(colorPicker.getValue());
            Root.getPortal().getTopbar().setStyle(Root.getPortal().getTopbar().getStyle().replaceAll("-fx-border-color: #......", "-fx-border-color: " + Colors.colorToHex(colorPicker.getValue())));
            Root.getPortal().getSideBar().setColor(colorPicker.getValue());
            Root.getPortal().topBarScrollBar.setStroke(Colors.highlightColor(colorPicker.getValue()));
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
        cfGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> User.active().setClock24Hour(newValue == twentyFour));
        if (User.active().isClock24Hour())
            twentyFour.setSelected(true);
        else twelve.setSelected(true);

        VBox clockFormat = new VBox(cfPrompt, twelve, twentyFour);
        clockFormat.setSpacing(10);
        clockFormat.setPadding(new Insets(10));
        layout.add(clockFormat, 0, 2);

        //Zip Code for Weather
        Text zcPrompt = new Text("Zip Code for Weather");
        int existingZC = User.active().getZipcode();
        TextField zcField = new TextField(existingZC == 0 ? "" : String.format("%05d", existingZC));
        zcField.setPrefColumnCount(5);
        zcField.lengthProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() > 5) {
                zcField.setText(zcField.getText().substring(0, 5));
            }
        });
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
                User.active().setLocation(Integer.parseInt(zcField.getText()));
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
            User.active().setTempUnits(newValue == celsius);
            Root.getPortal().updateWeatherDisplay();
        });

        if (User.active().usesFahrenheit())
            tuGroup.selectToggle(fahrenheit);
        else tuGroup.selectToggle(celsius);
        Text courtesy = new Text("Weather provided by NOAA");
        courtesy.setFont(Font.font("Sans Serif", FontPosture.ITALIC, 10));
        VBox temperatureUnits = new VBox(tuPrompt, fahrenheit, celsius, courtesy);
        temperatureUnits.setSpacing(10);
        temperatureUnits.setPadding(new Insets(10, 10, 10, 60));
        layout.add(temperatureUnits, 1, 2);

        //profile picture
        VBox profilePicture = new VBox();
        profilePicture.setSpacing(10);
        profilePicture.setPadding(new Insets(10, 10, 10, 10));

        Text ppPrompt = new Text("Profile Picture");
        Image image = User.active().getAcctImage();
        Shape picture = new ShapeImage(new Circle(30), image).apply();
        Button browse = new Button("Browse...");
        Text pictureInvalidMessage = new Text("");
        browse.setOnAction(event -> {
            pictureInvalidMessage.setText("");
            FileChooser fileChooser = new FileChooser();
            List<String> extensions = Arrays.asList("*.png", "*.jpeg", "*.jpg", "*.tiff", "*.gif", "*.webp", "*.svg", "*.bmp");
            fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("All Image Files", extensions));
            fileChooser.setTitle("Select Profile Picture");
            fileChooser.setInitialDirectory(new java.io.File(Address.fromRootAddr("resources")));
            java.io.File selected = fileChooser.showOpenDialog(Root.getPortal().getPrimaryStage());
            if (selected == null) {
                return;
            }
            Image new_image = new Image("file:" + selected.getPath());
            if (new_image.isError()) {
                pictureInvalidMessage.setText("Unable to parse " + selected.getName() + ".");
            } else {
                Paint fill = new ShapeImage(new Circle(30), new_image).apply().getFill();
                Root.getPortal().getPicture().setFill(fill);
                picture.setFill(fill);
                User.active().setImageFN(selected.getPath());
            }
        });
        Button resetToDefault = new Button("Reset to Default");
        resetToDefault.setOnAction(event -> {
            pictureInvalidMessage.setText("");
            User.active().setImageFN(new java.io.File(Address.fromRootAddr("resources", "default_user.png")).getPath());
            Image i = new Image("file:" + User.active().getImageFN());
            Paint fill = new ShapeImage(new Circle(30), i).apply().getFill();
            Root.getPortal().getPicture().setFill(fill);
            picture.setFill(fill);
        });
        profilePicture.getChildren().addAll(ppPrompt, picture, browse, resetToDefault, pictureInvalidMessage);

        layout.add(profilePicture, 0, 3);

        //image permissions

        Text ipPrompt = new Text("Profile picture visibility:");
        ToggleGroup ipGroup = new ToggleGroup();
        RadioButton justMe = new RadioButton("Just Me");
        RadioButton classmatesAndInstructorsOnly = new RadioButton("Classmates and Instructors Only");
        RadioButton everyone = new RadioButton("Everyone");
        justMe.setToggleGroup(ipGroup);
        classmatesAndInstructorsOnly.setToggleGroup(ipGroup);
        everyone.setToggleGroup(ipGroup);
        int oldIPValue = User.active().getPictureVisibility();
        ipGroup.selectToggle(oldIPValue == 0 ? justMe : oldIPValue == 1 ? classmatesAndInstructorsOnly : everyone);
        ipGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> User.active().setPictureVisibility(newValue == justMe ? 0 : newValue == classmatesAndInstructorsOnly ? 1 : 2));
        VBox imagePermissions = new VBox(ipPrompt, justMe, classmatesAndInstructorsOnly, everyone);
        imagePermissions.setSpacing(10);
        imagePermissions.setPadding(new Insets(10, 10, 10, 60));
        layout.add(imagePermissions, 1, 3);

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
