package gui;

import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import main.Root;
import main.Size;
import terminal.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Terminal extends AnchorPane {

    private ScrollPane wrapper;
    private ArrayList<String> previous_commands;
    private boolean pc_last_confirmed;
    private int pc_index;
    private int yindex;
    public static final int columns = 75;

    TranslateTransition ent;
    TranslateTransition exit;
    EventHandler<? super KeyEvent> next;
    TextField current;

    GridPane pane;
    TerminalUI eval;
    Main holder;

    public Terminal(Main holder, ScrollPane wrapper) {
        this.wrapper = wrapper;
        this.holder = holder;
        previous_commands = new ArrayList<>();

        TranslateTransition init = new TranslateTransition();
        init.setByY(Size.height(250));
        init.setByX(Size.width(-2000));
        init.setDuration(Duration.millis(1));
        init.setNode(this.wrapper);
        init.play();

        ent = new TranslateTransition();
        ent.setDuration(Duration.millis(200));
        ent.setNode(this.wrapper);
        ent.setAutoReverse(false);

        exit = new TranslateTransition();
        exit.setDuration(Duration.millis(200));
        exit.setNode(this.wrapper);
        exit.setAutoReverse(false);

        eval = new TerminalUI();
        init();
        setStyle("-fx-background-color: #202020");
        setPrefHeight(Size.height(600));
        setPrefWidth(Size.width(1200));
    }

    void start(){
        ent.setToX(Size.width(300));
        ent.play();
        if (current != null)
            current.setEditable(true);
    }

    void exit() {
        exit.setToX(Size.width(-2000));
        exit.play();
        current.setEditable(false);
    }

    private void advance(TextField field, KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            if (!pc_last_confirmed)
                previous_commands.remove(previous_commands.size() - 1);
            previous_commands.add(field.getText());
            pc_index = previous_commands.size();
            pc_last_confirmed = true;
            Task<TerminalRet> executeCommand = new Task<TerminalRet>() {
                @Override
                protected TerminalRet call() throws Exception {
                    return eval.command(field.getText(), 140) ;
                }
            };
            executeCommand.setOnSucceeded(event2 -> {
                TerminalRet terminalRet = executeCommand.getValue();
                String out = terminalRet.getText();
                // print output to the screen
                if (out.length() > 0) {
                    Text outputTXT = new Text(out);
                    outputTXT.setFill(Color.WHITE);
                    GridPane.setConstraints(outputTXT, 0, yindex++, 2, 1);
                    pane.getChildren().add(outputTXT);
                }

                if (!terminalRet.getEvents().getEvents().contains(TerminalDrivenEvent.CLEAR)) {
                    field.setEditable(false);
                    Text prompt_new = new Text(eval.getPrompt());
                    prompt_new.setFill(Color.WHITE);
                    TextField field_new = new TextField();
                    current = field_new;
                    field_new.setEditable(true);
                    field_new.setPrefColumnCount(columns);
                    field_new.setStyle("-fx-background-color: #202020; -fx-text-fill: #ffffff");
                    GridPane.setConstraints(prompt_new, 0, yindex, 1, 1);
                    pane.getChildren().add(prompt_new);
                    GridPane.setConstraints(field_new, 1, yindex++, 1, 1);
                    pane.getChildren().add(field_new);
                    field_new.requestFocus();
                    field_new.addEventHandler(KeyEvent.KEY_PRESSED, event1 -> {
                        if (Root.getPortal().getState() != Main.TERMINAL_STATE)
                            event1.consume();
                        else
                            advance(field_new, event1);
                    });
                    wrapper.setVvalue(wrapper.vmaxProperty().doubleValue());
                }

                //execute event(s) tied to command
                terminalRet.getEvents().run();
            });
            Thread executionThread = new Thread(executeCommand);
            executionThread.setDaemon(true);
            executionThread.start();
        }
        if (event.getCode().equals(KeyCode.UP)) {
            if (pc_index > 0) {
                if (pc_index == previous_commands.size()) {
                    previous_commands.add(field.getText());
                    pc_last_confirmed = false;
                }
                else if (pc_index == previous_commands.size() - 1 && !pc_last_confirmed) {
                    previous_commands.set(previous_commands.size() - 1, field.getText());
                }
                field.setText(previous_commands.get(--pc_index));
            }
        }
        if (event.getCode().equals(KeyCode.DOWN)) {
            if (pc_index < previous_commands.size() - 1) {
                field.setText(previous_commands.get(++pc_index));
            }
        }
        if (event.getCode().equals(KeyCode.TAB)) {
            int lastSpace = current.getText().lastIndexOf(" ") + 1;
            String before = current.getText().substring(0, lastSpace);
            String text = current.getText().substring(lastSpace);
            int lio = text.lastIndexOf(File.separator);
            String ssr = lio == -1 ? "." : text.substring(0, lio);
            try {
                File dir = Address.parse(ssr, true, false, true, false, false);
                String start = text.substring(lio + 1);
                String end = start;
                List<String> result = Files.walk(dir.toPath(), 1, FileVisitOption.FOLLOW_LINKS)
                        .filter(path -> !path.toFile().equals(dir))
                        .map(path -> path.toFile().getName())
                        .filter(name -> name.startsWith(start))
                        .collect(Collectors.toList());
                //added to prevent JVM crashes on tab when there are no file matches (somehow this bypasses the JVM error handler).
                if (result.size() == 0) {
                    current.requestFocus();
                    current.positionCaret(current.getText().length());
                    event.consume();
                    wrapper.setVvalue(wrapper.vmaxProperty().doubleValue());
                    return;
                }
                outer : for (int i = start.length();; i++) {
                    Character currentChar = null;
                    for (String name: result) {
                        if (name.length() <= i) {
                            break outer;
                        }
                        char test = name.charAt(i);
                        if (currentChar != null && !currentChar.equals(test))
                            break outer;
                        else
                            currentChar = test;
                    }
                    end += currentChar;
                }
                current.setText(before + (lio == -1 ? "" : (ssr + File.separator)) + end);
            } catch (TerminalException | IOException e) {
            } finally {
                current.requestFocus();
                current.positionCaret(current.getText().length());
                event.consume();
            }
        }
        wrapper.setVvalue(wrapper.vmaxProperty().doubleValue());
    }

    public void clearTerminal() {
        pane.getChildren().clear();
        init();
    }

    private void init() {
        pc_last_confirmed  = true;
        Text prompt = new Text(eval.getPrompt());
        prompt.setFill(Color.WHITE);
        TextField field = new TextField();
        current = field;
        field.setEditable(true);
        field.setPrefColumnCount(columns);
        field.setStyle("-fx-background-color: #202020; -fx-text-fill: #ffffff");
        pane = new GridPane();

        field.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (Root.getPortal().getState() != Main.TERMINAL_STATE)
                event.consume();
            else
                advance(field, event);
        });

        GridPane.setRowIndex(prompt, 0);
        GridPane.setColumnIndex(prompt, 0);
        GridPane.setRowIndex(field, 0);
        GridPane.setColumnIndex(field, 1);
        yindex = 1;

        pane.getChildren().addAll(prompt, field);
        pane.setPadding(Size.insets(10));
        getChildren().add(pane);
    }
}
