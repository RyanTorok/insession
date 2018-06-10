package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.PasswordField;
import javafx.scene.input.MouseEvent;
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
        oldPasswordWrapper.setPadding(new Insets(6));

        Text npPrompt = new Text("New Password  ");
        PasswordField newPassword = new PasswordField();
        newPassword.setPrefColumnCount(20);
        newPassword.setEditable(true);
        HBox newPasswordWrapper = new HBox(npPrompt, new Filler(), newPassword);
        newPasswordWrapper.setAlignment(Pos.CENTER);
        newPasswordWrapper.setPadding(new Insets(6));

        Text cfpPrompt = new Text("Confirm New Password  ");
        PasswordField cfPassword = new PasswordField();
        cfPassword.setPrefColumnCount(20);
        cfPassword.setEditable(true);
        HBox cfPasswordWrapper = new HBox(cfpPrompt, new Filler(), cfPassword);
        cfPasswordWrapper.setAlignment(Pos.CENTER);
        cfPasswordWrapper.setPadding(new Insets(6));

        Text invalidMsg = new Text("");

        Button submit = new Button("Submit");
        submit.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
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
            boolean success = false;
            if (error.length() == 0) {
                success = Root.getActiveUser().setPassword(newPassword.getText());
                if (!success) {
                    invalidMsg.setText("A connection error occurred. Please try again.");
                }
            }
        });

        VBox changePassword = new VBox(cpPrompt, oldPasswordWrapper, newPasswordWrapper, cfPasswordWrapper, invalidMsg, submit);
        changePassword.setAlignment(Pos.CENTER);
        changePassword.setPadding(new Insets(10));

        layout.add(changePassword, 0, 0);

        //accent color
        Text accentColorPrompt = new Text("Accent Color: ");
        Shape colorSquare = new Rectangle(60, 60);
        colorSquare.setFill(Root.getActiveUser().getAccentColor());
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(Root.getActiveUser().getAccentColor());
        colorPicker.setOnAction(event1 -> {
            colorSquare.setFill(colorPicker.getValue());
            Root.getActiveUser().setAccentColor(colorPicker.getValue());
            Root.getPortal().top_bar.setStyle(Root.getPortal().top_bar.getStyle().replaceAll("-fx-border-color: #......", "-fx-border-color: " + UtilAndConstants.colorToHex(colorPicker.getValue())));
            Root.getPortal().sideBar.setColor(colorPicker.getValue());
        });
        VBox selectColor = new VBox(colorSquare, colorPicker);
        selectColor.setPadding(new Insets(0, 10, 10, 10));
        selectColor.setAlignment(Pos.TOP_CENTER);
        HBox accentColor = new HBox(accentColorPrompt, selectColor);
        accentColor.setPadding(new Insets(10));
        layout.add(accentColor, 1, 0);

        Button closeBtn = new Button("Close");
        closeBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            this.close();
        });
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
