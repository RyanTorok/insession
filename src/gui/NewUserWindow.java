package gui;

import classes.SQL;
import db.LoginException;
import db.SQLMaster;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import main.*;

import java.sql.*;
import java.util.ArrayList;

public class NewUserWindow extends Pane {

    private ArrayList<Entry> entries = new ArrayList<>();
    private static User user = null;
    private byte which = 0; //select account = 0, login = 1, create account = 2
    private Pane pane;
    private boolean loginConnErr = false;

    private NewUserWindow(Main main) {
        super();
        Text mainlogo = new Text("paintbrush.");
        mainlogo.setFill(Color.WHITE);
        mainlogo.setFont(Font.font("Comfortaa",FontWeight.NORMAL, 80));
        VBox pane = new VBox();
        VBox subpane = new VBox();

        ArrayList<Entry> entries1 = new ArrayList<>();
        entries1.add(new Entry("Confirm Password ", 2, true));
        entries1.add(new Entry("First Name             ", 3, false));
        entries1.add(new Entry("Last Name             ", 4, false));
        entries1.add(new Entry("Email                      ", 5, false));
        entries1.add(new Entry("School Code          ", 6, false));

        Text invalidMessage = new Text("");
        invalidMessage.setFont(Font.font("Comfortaa", 20));
        invalidMessage.setFill(Color.WHITE);
        Button b = new Button("Submit");
        b.setAlignment(Pos.CENTER);
        pane.getChildren().add(subpane);
        b.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            invalidMessage.setText("");
            if (which == 1) {
                User user = dbLookup(entries.get(0).getField(), entries.get(1).getField());
                if (user != null) {
                    Root.setActiveUser(user);
                    main.switchToMain();
                } else {
                    if (loginConnErr)
                        invalidMessage.setText("A connection error occurred. Please try again.");
                    else
                    invalidMessage.setText("We do not recognize that username and password combination.");
                    loginConnErr = false;
                }
            } else {
                String  username   = entries .get(0).getField(),
                        password   = entries .get(1).getField(),
                        passwordC  = entries1.get(0).getField(),
                        first      = entries1.get(1).getField(),
                        last       = entries1.get(2).getField(),
                        email      = entries1.get(3).getField(),
                        schoolCode = entries1.get(4).getField();
                boolean valid = true;
                valid = valid && checkEmptyField(username, "Username", invalidMessage);
                valid = valid && checkEmptyField(password, "Password", invalidMessage);
                valid = valid && checkEmptyField(passwordC, "Password", invalidMessage);
                valid = valid && checkEmptyField(first, "First Name", invalidMessage);
                valid = valid && checkEmptyField(last, "Last Name", invalidMessage);
                valid = valid && checkEmptyField(email, "Email", invalidMessage);
                if (!valid)
                    return;
                if (!(password.equals(passwordC))) {
                    invalidMessage.setText("Your passwords do not match. Check your spelling.");
                    return;
                }
                int success = dbCreate(username, password, first, last, email, schoolCode);
                if (success == -1) {
                    invalidMessage.setText("Your username is already in use. Please choose another username.");
                    return;
                }
                if (success == -2) {
                    invalidMessage.setText("A connection error occurred. Please try again.");
                    return;
                }
                int type = success;
                assert type == 0 || type == 1 || type == 2;

                User newUser = null;
                switch (type) {
                    case 0: newUser = new Student(Root.getMACAddress(), username, password, first, null, last, email, new Timestamp(System.currentTimeMillis()), null, -1);
                    break;
                    case 1: newUser = new Teacher(Root.getMACAddress(), username, password, first, null, last, email, null, new Timestamp(System.currentTimeMillis()));
                    break;
                    case 2: newUser = new Administrator(Root.getMACAddress(), username, password, first, null, last, email, null, new Timestamp(System.currentTimeMillis()));
                    break;
                }
                user = newUser;
                Root.setActiveUser(newUser);
                main.switchToMain();
            }
        });

        //existing accounts pane
        VBox existingAccts = new VBox();
        ArrayList<User> users = User.readAll();
        if (users != null) {
            for (User u: users) {
                Text user_name = new Text(u.getFirst() + " " + u.getLast());
                user_name.setFill(Color.WHITE);
                user_name.setFont(Font.font("Sans Serif", FontWeight.NORMAL, 20));
                Text user_username = new Text(u.getUsername());
                user_username.setFill(Color.WHITE);
                user_username.setFont(Font.font("Sans Serif", FontPosture.ITALIC, 20));
                VBox names = new VBox(user_name, user_username);
                FlowPane userPane = new FlowPane(names);
                userPane.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
                    userPane.setStyle("-fx-background-color: #505050");
                });
                userPane.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
                    userPane.setStyle("-fx-background-color: #000000");
                });
                userPane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    user = u;
                    Root.setActiveUser(u);
                    main.switchToMain();
                });
                userPane.setPadding(new Insets(20,400,20,40));
                userPane.setPrefWidth(550);
                existingAccts.getChildren().add(userPane);
            }
        }

        pane.setPadding(new Insets(80));
        subpane.setSpacing(20);
        pane.setSpacing(20);
        subpane.getChildren().add(mainlogo);
        Text sa = new Text("Select Account");
        sa.setFont(Font.font("Comfortaa", FontWeight.NORMAL, 20));
        sa.setFill(Color.WHITE);
        FlowPane sacct = new FlowPane(sa);
        sacct.setStyle("-fx-background-color: #505050");
        Text li = new Text("Log In");
        li.setFont(Font.font("Comfortaa", FontWeight.NORMAL, 20));
        li.setFill(Color.WHITE);
        FlowPane login = new FlowPane(li);
        sacct.setStyle("-fx-background-color: #505050");
        Text cr = new Text("Create Account");
        cr.setFont(Font.font("Comfortaa", FontWeight.NORMAL, 20));
        cr.setFill(Color.WHITE);
        FlowPane create = new FlowPane(cr);
        create.setStyle("-fx-background-color: #000000");
        sacct.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            sacct.setStyle("-fx-background-color: #505050");
        });
        sacct.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            if (which != 0) {
                sacct.setStyle("-fx-background-color: #000000");
            }
        });
        sacct.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            invalidMessage.setText("");
            sacct.setStyle("-fx-background-color: #505050");
            login.setStyle("-fx-background-color: #000000");
            create.setStyle("-fx-background-color: #000000");
            if (which != 0) {
                subpane.getChildren().add(existingAccts);
                subpane.getChildren().removeAll(entries);
                subpane.getChildren().removeAll(entries1);
                pane.getChildren().remove(b);
            }
            which = 0;
        });
        login.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            login.setStyle("-fx-background-color: #505050");
        });
        login.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            if (which != 1) {
                login.setStyle("-fx-background-color: #000000");
            }
        });
        login.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            invalidMessage.setText("");
            login.setStyle("-fx-background-color: #505050");
            sacct.setStyle("-fx-background-color: #000000");
            create.setStyle("-fx-background-color: #000000");
            if (which == 0) {
                subpane.getChildren().remove(existingAccts);
                subpane.getChildren().addAll(entries);
                pane.getChildren().add(b);
            }
            if (which == 2)
                subpane.getChildren().removeAll(entries1);
            which = 1;
        });
        create.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            create.setStyle("-fx-background-color: #505050");
        });
        create.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            if (which != 2) {
                create.setStyle("-fx-background-color: #000000");
            }
        });
        create.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            invalidMessage.setText("");
            create.setStyle("-fx-background-color: #505050");
            sacct.setStyle("-fx-background-color: #000000");
            login.setStyle("-fx-background-color: #000000");
            if (which == 0) {
                subpane.getChildren().remove(existingAccts);
                subpane.getChildren().addAll(entries);
                subpane.getChildren().addAll(entries1);
                pane.getChildren().add(b);
            }
            if (which == 1)
                subpane.getChildren().addAll(entries1);
            which = 2;

        });
        sacct.setPadding(new Insets(20));
        login.setPadding(new Insets(20));
        create.setPadding(new Insets(20));
        sa.setTextAlignment(TextAlignment.CENTER);
        li.setTextAlignment(TextAlignment.CENTER);
        cr.setTextAlignment(TextAlignment.CENTER);
        sacct.setPrefWidth(325);
        login.setPrefWidth(325);
        create.setPrefWidth(325);
        sacct.setAlignment(Pos.CENTER);
        login.setAlignment(Pos.CENTER);
        create.setAlignment(Pos.CENTER);
        HBox select = new HBox(sacct, login, create);
        select.setAlignment(Pos.CENTER);
        subpane.getChildren().add(select);
        subpane.getChildren().add(existingAccts);
        entries.add(new Entry("Username              ", 0, false));
        entries.add(new Entry("Password               ", 1, true));


        subpane.setAlignment(Pos.CENTER);
        pane.getChildren().add(invalidMessage);

        pane.setStyle("-fx-background-color: #000000");
        pane.setAlignment(Pos.TOP_CENTER);
        this.pane = pane;
    }


    private boolean checkEmptyField(String toCheck, String name, Text invalidMessage) {
        if (toCheck.length() == 0) {
            if (invalidMessage.getText().length() == 0)
                invalidMessage.setText(name + " cannot be blank.");
            return false;
        }
        return true;
    }

    private User dbLookup(String username, String password) {
        try {
            SQLMaster.connectToOverallServer();
        } catch (SQLException e) {
            loginConnErr = true;
            return null;
        }
        try {
            return SQLMaster.login(username, password);
        } catch (LoginException e) {
            loginConnErr = e.getMessage().equals("true");
            return null;
        }
    }

    private int dbCreate(String username, String password, String first, String last, String email, String schoolCode) {
        try {
            SQLMaster.connectToOverallServer();
        } catch (SQLException e) {
            return -2;
        }
        return SQLMaster.createAccount(username, password, first, last, email, schoolCode);
    }

    public static User getUser() {
        return user;
    }

    private class Entry extends HBox {

        private int index;
        TextField box;

        private Entry(String item, int index, boolean hide) {
            this(item, new TextField(), index, hide);
        }
        private Entry(String item, TextField entryBox, int index, boolean hide) {
            if (hide) {
                entryBox = new PasswordField();
            }
            this.index = index;
            this.box = entryBox;
            Text prompt = new Text(item);
            prompt.setFill(Color.WHITE);
            prompt.setFont(Font.font("Comfortaa", FontWeight.NORMAL, 20));
            entryBox.setPrefColumnCount(20);
            getChildren().addAll(prompt, entryBox);
            setSpacing(30);
        }
        int getIndex() {
            return index;
        }

        public String getField() {
            return box.getText();
        }
    }
    public static Scene get(Main main) {
        return new Scene(new NewUserWindow(main).pane, 650, 700);
    }
}
