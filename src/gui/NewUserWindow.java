package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.*;
import main.*;
import net.Login;
import net.Net;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
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
        b.setOnAction(event -> {
            invalidMessage.setText("");
            if (which == 1) {
                Login login = new Login(entries.get(0).getField(), entries.get(1).getField(), true);
                login.setOnSucceeded(event1 -> {
                    Net.UserMaybe userMaybe = (Net.UserMaybe) login.getValue();
                    switch (userMaybe.getExistsCode()) {
                        case -2: {
                            //expected user and got correct input, but missing ser file
                            invalidMessage.setText("We cannot locate your user data. Try creating a new account.");
                        } break;
                        case -1: invalidMessage.setText("A connection error occurred. Please try again."); break;
                        case 0:
                            invalidMessage.setText("We do not recognize that username and password combination.");
                            loginConnErr = false;
                            break;
                        case 1: invalidMessage.setText("You shouldn't be able to see this. If you do, it's a bug.");
                            break;
                        case 2: {
                            //valid user login
                            User.setActive(userMaybe.getUser());
                            Root.getPortal().switchToMain();
                        }
                    }
                });
                Thread th = new Thread(login);
                th.setDaemon(true);
                th.start();
            } else {
                String  username   = entries .get(0).getField(),
                        password   = entries .get(1).getField(),
                        passwordC  = entries1.get(0).getField(),
                        first      = entries1.get(1).getField(),
                        last       = entries1.get(2).getField(),
                        email      = entries1.get(3).getField(),
                        schoolCode = entries1.get(4).getField();
                boolean valid;
                valid = checkEmptyField(username, "Username", invalidMessage);
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
                int type = 0;
                net.Net.CreateAccountStatus status = net.Net.createAccount(username, password, first, last, email, schoolCode);
                type = status.getStatus();
                User newUser = null;
                switch (type) {
                    case -3: invalidMessage.setText("The school code you entered was not recognized.");
                    return;
                    case -2: invalidMessage.setText("A connection error occurred. Please try again.");
                    return;
                    case -1: invalidMessage.setText("Your username is already in use. Please choose another username."); //deprecated: duplicate usernames are allowed.
                    return;
                    case 0: newUser = new Student(Root.getMACAddress(), username, password.getBytes(StandardCharsets.UTF_8), first, null, last, email, new Timestamp(System.currentTimeMillis()), null, -1);
                    break;
                    case 1: newUser = new Teacher(Root.getMACAddress(), username, password.getBytes(StandardCharsets.UTF_8), first, null, last, email, null, new Timestamp(System.currentTimeMillis()));
                    break;
                    case 2: newUser = new Administrator(Root.getMACAddress(), username, password.getBytes(StandardCharsets.UTF_8), first, null, last, email, null, new Timestamp(System.currentTimeMillis()));
                    break;
                }
                user = newUser;
                newUser.setPassword(status.getPasswordCombo().getEncryptedPassword());
                newUser.setPasswordSalt(status.getPasswordCombo().getSalt());
                User.setActive(newUser);
                main.switchToMain();
            }
        });
        boolean existingUser = false;
        //existing accounts pane
        VBox existingAccts = new VBox();
        ArrayList<User> users = User.readAll();
        User guest = new Student(null, null, null, "Preview as Guest", null, "", null, new Timestamp(System.currentTimeMillis()), null, -1);
        guest.setAccentColor(Color.web("#505050"));
        users.add(guest);
        if (users != null) {
            for (User u: users) {
                existingUser = existingUser || !u.getFirst().equals("Preview as Guest");
                Text user_name = new Text(u.getFirst() + " " + u.getLast());
                Color backgd = u.getAccentColor();
                Color whiteBlack = Colors.textFill(backgd, 2);
                user_name.setFill(whiteBlack);
                user_name.setFont(Font.font("Sans Serif", FontWeight.NORMAL, 20));
                Text user_username = new Text(u.getUsername());
                user_username.setFill(whiteBlack);
                user_username.setFont(Font.font("Sans Serif", FontPosture.ITALIC, 20));
                VBox names = new VBox(user_name, user_username);
                HBox userPane = new HBox(names);
                Color lightBackgd = Colors.highlightColor(backgd);
                final String bgcStr = Colors.colorToHex(backgd);
                final String lbgStr = Colors.colorToHex(lightBackgd);
                userPane.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> userPane.setStyle("-fx-background-color: " + lbgStr));
                userPane.addEventHandler(MouseEvent.MOUSE_EXITED, event -> userPane.setStyle("-fx-background-color: " + bgcStr));
                userPane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    user = u;
                    User.setActive(u);
                    main.switchToMain();
                });
                userPane.setStyle("-fx-background-color: " + bgcStr);
                userPane.setPadding(new Insets(20,40,20,40));
                userPane.setPrefWidth(550);
                existingAccts.getChildren().add(userPane);
                //picture
                Image image = u.getAcctImage();
                Region filler = new Region();
                HBox.setHgrow(filler, Priority.ALWAYS);
                Shape picture;
                if (image != null)
                    picture = new ShapeImage(new Circle(30), image).apply();
                else
                    picture = new Circle(30, Color.color(1,0,0,0));
                userPane.getChildren().addAll(filler ,picture);
            }
        }

        pane.setPadding(new Insets(80, 0, 80, 0));
        subpane.setSpacing(20);
        pane.setSpacing(20);
        subpane.getChildren().add(mainlogo);
        Text sa = new Text("Select Account");
        sa.setFont(Font.font("Comfortaa", FontWeight.NORMAL, 20));
        sa.setFill(Color.WHITE);
        FlowPane sacct = new FlowPane(sa);
        sacct.setStyle("-fx-background-color: #4d4d4d");
        Text li = new Text("Log In");
        li.setFont(Font.font("Comfortaa", FontWeight.NORMAL, 20));
        li.setFill(Color.WHITE);
        FlowPane login = new FlowPane(li);
        sacct.setStyle("-fx-background-color: #4d4d4d");
        Text cr = new Text("Create Account");
        cr.setFont(Font.font("Comfortaa", FontWeight.NORMAL, 20));
        cr.setFill(Color.WHITE);
        FlowPane create = new FlowPane(cr);
        create.setStyle("-fx-background-color: #000000");
        sacct.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            sacct.setStyle("-fx-background-color: #4d4d4d");
        });
        sacct.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            if (which != 0) {
                sacct.setStyle("-fx-background-color: #000000");
            }
        });
        sacct.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            invalidMessage.setText("");
            sacct.setStyle("-fx-background-color: #4d4d4d");
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
            login.setStyle("-fx-background-color: #4d4d4d");
        });
        login.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            if (which != 1) {
                login.setStyle("-fx-background-color: #000000");
            }
        });
        login.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            invalidMessage.setText("");
            login.setStyle("-fx-background-color: #4d4d4d");
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
            create.setStyle("-fx-background-color: #4d4d4d");
        });
        create.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            if (which != 2) {
                create.setStyle("-fx-background-color: #000000");
            }
        });
        create.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            invalidMessage.setText("");
            create.setStyle("-fx-background-color: #4d4d4d");
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

        //pad entries

        subpane.setAlignment(Pos.CENTER);
        pane.getChildren().add(invalidMessage);

        pane.setStyle("-fx-background-color: #000000");
        pane.setAlignment(Pos.TOP_CENTER);
        if (!existingUser) {
            login.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED,
                    0, 0, 0, 0, MouseButton.PRIMARY, 1,
                    true, true, true, true, true, true, true, true, true, true, null));
            select.getChildren().remove(sacct);
        }
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
            setPadding(new Insets(0, 0, 0, 80));
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
