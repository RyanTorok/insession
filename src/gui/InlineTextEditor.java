package gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.*;
import main.*;
import org.fxmisc.richtext.InlineCssTextArea;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class InlineTextEditor extends VBox {

    private BooleanSupplier[] submitConditions;
    private Node target;
    private final TextFlow source;
    private final Parent parent;
    private final int index;
    private Consumer<CompressedRichText> onFinished;
    private StackPane controls;
    private InlineCssTextArea editor;
    private boolean styleEdited;
    private boolean overrideCaretHandler;
    private final ChoiceBox<String> fontSelector;
    private final Spinner<Double> fontSizeSelector;
    private OperatorButton bold = null;
    private OperatorButton italic = null;
    private OperatorButton underline = null;
    private OperatorButton strikethrough = null;
    private OperatorButton symbol = null;
    private OperatorButton math = null;
    private KeyMap keyMap;
    private OperatorButton submit = null;

    public static void edit(Node target, TextFlow source) {
        edit(target, source, onFinished->{});
    }

    public static void edit(Node target, TextFlow source, Consumer<CompressedRichText> onFinished, BooleanSupplier... submitConditions) {
        Parent parent = target.getParent();
        if (!(parent instanceof Pane))
            throw new IllegalCallerException("Parent of target node to edit inline does not extend Pane");
        int index = parent.getChildrenUnmodifiable().indexOf(target);
        if (index == -1)
            throw new IllegalArgumentException("Parent does not contain node of target to edit inline");
        InlineTextEditor editor = new InlineTextEditor(target, source, parent, index, onFinished, submitConditions);
        ((Pane) parent).getChildren().set(index, editor);
        User.active().getKeyMap().lock();
        editor.keyMap.unlock();

    }


    public InlineTextEditor(Node target, TextFlow source, Parent parent, int index, Consumer<CompressedRichText> onFinished, BooleanSupplier... submitConditions) {
        super();
        this.target = target;
        this.source = source;
        this.parent = parent;
        this.index = index;
        this.onFinished = onFinished;
        this.submitConditions = submitConditions;
        styleEdited = false;

        var alignments = new Object() {
            OperatorButton leftAlign = null;
            OperatorButton centerAlign = null;
            OperatorButton rightAlign = null;
            OperatorButton justifyAlign = null;
        };

        keyMap = new KeyMap();
        keyMap.associate(Main.BASE_STATE, false, "Ctrl+B", event-> {
            Events.fireMouse(bold, MouseEvent.MOUSE_CLICKED);
        });
        keyMap.associate(Main.BASE_STATE, false, "Ctrl+I", event-> {
            Events.fireMouse(italic, MouseEvent.MOUSE_CLICKED);
        });
        keyMap.associate(Main.BASE_STATE, false, "Ctrl+U", event-> {
            Events.fireMouse(underline, MouseEvent.MOUSE_CLICKED);
        });
        keyMap.associate(Main.BASE_STATE, false, "Ctrl+Alt+S", event-> {
            Events.fireMouse(strikethrough, MouseEvent.MOUSE_CLICKED);
        });
        keyMap.associate(Main.BASE_STATE, false, "Ctrl+M", event-> {
            Events.fireMouse(math, MouseEvent.MOUSE_CLICKED);
        });
        keyMap.associate(Main.BASE_STATE, false, "Ctrl+L", event-> {
            Events.fireMouse(alignments.leftAlign, MouseEvent.MOUSE_CLICKED);
        });
        keyMap.associate(Main.BASE_STATE, false, "Ctrl+E", event-> {
            Events.fireMouse(alignments.centerAlign, MouseEvent.MOUSE_CLICKED);
        });
        keyMap.associate(Main.BASE_STATE, false, "Ctrl+R", event-> {
            Events.fireMouse(alignments.rightAlign, MouseEvent.MOUSE_CLICKED);
        });
        keyMap.associate(Main.BASE_STATE, false, "Ctrl+J", event-> {
            Events.fireMouse(alignments.justifyAlign, MouseEvent.MOUSE_CLICKED);
        });
        keyMap.associate(Main.BASE_STATE, false, "Ctrl+S", event-> {
            Events.fireMouse(submit, MouseEvent.MOUSE_CLICKED);
        });
        keyMap.lock();

        Root.getPortal().getPrimaryStage().addEventHandler(KeyEvent.ANY, event -> {
            keyMap.fireEvent(event, Root.getPortal().getState(), Root.getPortal().isHomeScreen());
        });

        Styles.setBackgroundColor(this, Color.LIGHTGRAY);
        setPadding(Size.insets(20));
        setSpacing(Size.height(10));

        HBox textControls = new HBox();
        Styles.setBackgroundColor(textControls, Color.LIGHTGRAY);
        textControls.setPadding(Size.insets(15));

        double fontSize = Size.fontSize(24);

        fontSelector = new ChoiceBox<>(FXCollections.observableArrayList(Font.getFamilies()));
        fontSelector.setValue(Font.getDefault().getFamily());
        fontSelector.setMaxHeight(Size.height(500));
        fontSelector.valueProperty().addListener((observable, oldValue, newValue) -> styleSelected("-fx-font-family : " + newValue));

        fontSizeSelector = new Spinner<>(new SpinnerValueFactory<>() {
            @Override
            public void decrement(int steps) {
                Double val = getValue();
                if (val == null)
                    val = 0.0;
                setValue(Math.max(0, Math.ceil(val - steps)));
            }

            @Override
            public void increment(int steps) {
                Double val = getValue();
                if (val == null)
                    val = 0.0;
                setValue(Math.floor(val + steps));
            }
        });
        fontSizeSelector.increment(12);
        fontSizeSelector.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                newValue = 12.0;
            newValue = Math.round(2 * newValue) / 2.0; //round to X.0 or X.5
            styleSelected("-fx-font-size : " + newValue + "pt");
        });
        fontSizeSelector.setPrefSize(Size.width(80), Size.height(35));

        //bold button
        bold = new OperatorButton("B", Font.font("DejaVu Sans Mono", FontWeight.BOLD, fontSize), (on)-> {
            styleSelected("-fx-font-weight", on, "bold", "normal");
        });

        //italic button
        italic = new OperatorButton("I", Font.font("DejaVu Sans Mono", FontPosture.ITALIC, fontSize),(on)-> {
            styleSelected("-fx-font-style", on, "italic", "normal");
        });

        //underline button
        underline = new OperatorButton("U", Font.font("DejaVu Sans Mono", fontSize),(on)-> {
            styleSelected("-fx-underline", on, "true", "false");
        });
        underline.text.setUnderline(true);

        //strikethrough button
        strikethrough = new OperatorButton("S", Font.font("DejaVu Sans Mono", fontSize), (on)-> {
            styleSelected("-fx-strikethrough", on, "true", "false");
        });
        strikethrough.text.setStrikethrough(true);

        //symbol button
        symbol = new OperatorButton(Character.toString((char) 0x3a9), Font.font("DejaVu Sans Mono", fontSize), (on)-> {

        });

        //math equation button
        math = new OperatorButton(Character.toString((char) 0xf7), Font.font("DejaVu Sans Mono", fontSize), (on)-> {

        });


        //left align button
        alignments.leftAlign = new OperatorButton("", Font.font("DejaVu Sans Mono", fontSize), (on)-> {
            if(on) {
                if (!alignments.centerAlign.on && !alignments.rightAlign.on && !alignments.justifyAlign.on)
                    Events.fireMouse(alignments.rightAlign, MouseEvent.MOUSE_CLICKED);
                styleSelectedParagraph("-fx-text-alignment: left");
                if (alignments.centerAlign.on)
                    Events.fireMouse(alignments.centerAlign, MouseEvent.MOUSE_CLICKED);
                if (alignments.rightAlign.on)
                    Events.fireMouse(alignments.rightAlign, MouseEvent.MOUSE_CLICKED);
                if (alignments.justifyAlign.on)
                    Events.fireMouse(alignments.justifyAlign, MouseEvent.MOUSE_CLICKED);
            }
        });
        alignments.leftAlign.softEnable();
        alignments.leftAlign.getChildren().add(new AlignmentLines(0));

        //center align button
        alignments.centerAlign = new OperatorButton("", Font.font("DejaVu Sans Mono", fontSize), (on)-> {
            if(on) {
                if (!alignments.leftAlign.on && !alignments.rightAlign.on && !alignments.justifyAlign.on)
                    Events.fireMouse(alignments.centerAlign, MouseEvent.MOUSE_CLICKED);
                styleSelectedParagraph("-fx-text-alignment: center");
                if (alignments.leftAlign.on)
                    Events.fireMouse(alignments.leftAlign, MouseEvent.MOUSE_CLICKED);
                if (alignments.rightAlign.on)
                    Events.fireMouse(alignments.rightAlign, MouseEvent.MOUSE_CLICKED);
                if (alignments.justifyAlign.on)
                    Events.fireMouse(alignments.justifyAlign, MouseEvent.MOUSE_CLICKED);
            }
        });
        alignments.centerAlign.getChildren().add(new AlignmentLines(1));

        //right align button
        alignments.rightAlign = new OperatorButton("", Font.font("DejaVu Sans Mono", fontSize), (on)-> {
            if(on) {
                if (!alignments.leftAlign.on && !alignments.centerAlign.on && !alignments.justifyAlign.on)
                    Events.fireMouse(alignments.rightAlign, MouseEvent.MOUSE_CLICKED);
                styleSelectedParagraph("-fx-text-alignment: right");
                if (alignments.leftAlign.on)
                    Events.fireMouse(alignments.leftAlign, MouseEvent.MOUSE_CLICKED);
                if (alignments.centerAlign.on)
                    Events.fireMouse(alignments.centerAlign, MouseEvent.MOUSE_CLICKED);
                if (alignments.justifyAlign.on)
                    Events.fireMouse(alignments.justifyAlign, MouseEvent.MOUSE_CLICKED);
            }
        });
        alignments.rightAlign.getChildren().add(new AlignmentLines(2));

        //justify align button
        alignments.justifyAlign = new OperatorButton("", Font.font("DejaVu Sans Mono", fontSize), (on)-> {
            if (on) {
                styleSelectedParagraph("-fx-text-alignment: justify");
                if (!alignments.leftAlign.on && !alignments.centerAlign.on && !alignments.rightAlign.on)
                    Events.fireMouse(alignments.justifyAlign, MouseEvent.MOUSE_CLICKED);
                if (alignments.leftAlign.on)
                    Events.fireMouse(alignments.leftAlign, MouseEvent.MOUSE_CLICKED);
                if (alignments.centerAlign.on)
                    Events.fireMouse(alignments.centerAlign, MouseEvent.MOUSE_CLICKED);
                if (alignments.rightAlign.on)
                    Events.fireMouse(alignments.rightAlign, MouseEvent.MOUSE_CLICKED);
            }
        });
        alignments.justifyAlign.getChildren().add(new AlignmentLines(4));

        //double space button
        OperatorButton doubleSpace = new OperatorButton("", Font.font("DejaVu Sans Mono", fontSize), (on) -> {
        });

        //text color chooser
        ColorPicker textColor = new ColorPicker();
        textColor.valueProperty().addListener((observable, oldValue, newValue) -> {
            int red = (int) (newValue.getRed() * 255);
            int green = (int) (newValue.getGreen() * 255);
            int blue = (int) (newValue.getBlue() * 255);
            styleSelected("-fx-text-fill : rgb(" + red + ", " + green + ", "  + blue + ")");
            if (editor.getSelection().getLength() != 0)
                styleEdited = true;
        });

        //highlight color chooser
        ColorPicker highlightColor = new ColorPicker();
        highlightColor.valueProperty().addListener((observable, oldValue, newValue) -> {
            int red = (int) (newValue.getRed() * 255);
            int green = (int) (newValue.getGreen() * 255);
            int blue = (int) (newValue.getBlue() * 255);
            styleSelected("-fx-highlight-fill : rgb(" + red + ", " + green + ", "  + blue + ")");
            if (editor.getSelection().getLength() != 0)
                styleEdited = true;
        });

        OperatorButton reset = new OperatorButton(Character.toString((char) 0x21ba), Font.font(fontSize), (on) -> {
            editor.clear();
            initEditorBody();
            styleEdited = false;
        }, false, true, "Reset? Click again to confirm.");

        //cancel button
        OperatorButton cancel = new OperatorButton(Character.toString((char) 0xd7), Font.font(fontSize), (on) -> cancel(), false, true, "Cancel? Click again to confirm.");

        //submit button
        submit = new OperatorButton(Character.toString((char) 0x2714), Font.font(fontSize), (on)-> submit(), false, false, null);

        HBox mainControls = new HBox(fontSelector, fontSizeSelector, bold, italic, underline, strikethrough, symbol, math, alignments.leftAlign, alignments.centerAlign, alignments.rightAlign, alignments.justifyAlign, doubleSpace, textColor, highlightColor, new Layouts.Filler(), reset, cancel, submit);
        mainControls.setSpacing(Size.width(5));
        controls = new StackPane(mainControls);
        Styles.setBackgroundColor(controls, Color.LIGHTGRAY);

        initEditorBody();

        editor.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                User.active().getKeyMap().lock();
                keyMap.unlock();
            } else {
                User.active().getKeyMap().unlock();
                keyMap.lock();
            }
        });

        editor.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() < oldValue.length() && emptySelection()) {
                //we only deleted text
                return;
            }
            if (newValue.length() > oldValue.length() + 1) {
                //paste in text, just ignore the formatting.
                return;
            }
            int charAt = Math.max(selectedStart() - 1, 0);
            restyleCharacter(charAt, currentStyle());
        });
        editor.caretPositionProperty().addListener((observable, oldValue, newValue) -> {
            if (overrideCaretHandler) {
                overrideCaretHandler = false;
                return;
            }
            int indexToCheck = editor.getSelection().getLength() > 0 ? selectedStart() : Math.max(0, newValue - 1);
            Text dummyNode = new Text();
            dummyNode.setStyle(editor.getStyleOfChar(indexToCheck));

            if (Styles.getProperty(dummyNode, "-fx-font-weight").contains("bold"))
                bold.softEnable();
            else bold.softDisable();

            if (Styles.getProperty(dummyNode, "-fx-font-style").equals("italic"))
                italic.softEnable();
            else italic.softDisable();

            if (Styles.getProperty(dummyNode, "-fx-underline").equals("true"))
                underline.softEnable();
            else underline.softDisable();

            if (Styles.getProperty(dummyNode, "-fx-strikethrough").equals("true"))
                strikethrough.softEnable();
            else strikethrough.softDisable();

            alignments.leftAlign.softDisable();
            alignments.centerAlign.softDisable();
            alignments.rightAlign.softDisable();
            alignments.justifyAlign.softDisable();

            String alignment = Styles.getProperty(dummyNode, "-fx-text-alignment");
            if (alignment.length() > 0) {
                switch (alignment) {
                    case "left":
                        alignments.leftAlign.softEnable();
                        break;
                    case "center":
                        alignments.centerAlign.softEnable();
                        break;
                    case "right":
                        alignments.rightAlign.softEnable();
                        break;
                    case "justify":
                        alignments.justifyAlign.softEnable();
                        break;
                    default: break;
                }
            }

            boolean allSameFont = true;
            boolean allSameSize = true;
            String fontFamily = Styles.getProperty(dummyNode, "-fx-font-family");
            String fontSz = Styles.getProperty(dummyNode, "-fx-font-size");
            if (fontSz.length() == 0)
                fontSz = "12.0";
            if (fontSz.endsWith("pt") || fontSz.endsWith("px") || fontSz.endsWith("em"))
                fontSz = fontSz.substring(0, fontSz.length() - 2);

            for (int i = selectedStart(); i < selectedEnd(); i++) {
                Text dummy = new Text();
                dummy.setStyle(editor.getStyleOfChar(i));
                if (!(fontFamily.equals(Styles.getProperty(dummy, "-fx-font-family")))) allSameFont = false;
                if (!(fontSz.equals(Styles.getProperty(dummy, "-fx-font-size")))) allSameSize = false;
            }

            if (allSameFont) {
                fontSelector.setValue(fontFamily);
            }
            if (allSameSize) {
                double diff = Double.parseDouble(fontSz) - fontSizeSelector.valueProperty().get();
                if (diff < 0) fontSizeSelector.decrement((int) Math.abs(Math.floor(diff)));
                else fontSizeSelector.increment((int) Math.ceil(diff));
            }
        });
        getChildren().addAll(controls, editor);
    }

    private boolean emptySelection() {
        return selectedStart() == selectedEnd();
    }

    private void initEditorBody() {
        int index = getChildren().indexOf(editor);
        editor = new InlineCssTextArea(source.getChildren().stream().map(text -> {
            if (text instanceof Text)
                return ((Text) text).getText();
            else return "";
        }).collect(Collectors.joining()));

        int charAt = 0;
        for (Node n : source.getChildren()) {
            if (!(n instanceof Text))
                return;
            Text t = ((Text) n);
            final int length = t.getText().length();
            editor.setStyle(charAt, charAt + length, t.getStyle());
            charAt += length;
        }

        if (index != -1) {
            getChildren().set(index, editor);
        }
    }

    private void cancel() {
        ((Pane) parent).getChildren().set(index, target);
        keyMap.lock();
        Root.getPortal().getKeyMap().unlock();
        onFinished.accept(null);
    }

    private void styleSelectedParagraph(String s) {
        styleEdited = true;
        for (int i = 0; i < editor.getParagraphs().size(); i++) {
            IndexRange selectedRange = editor.getParagraphSelection(i);
            System.out.println(selectedRange);
            if (selectedRange.getLength() > 0)
                editor.setStyle(selectedRange.getStart(), selectedRange.getEnd(), s);
        }
    }

    private void styleCharacter(int index, String style) {
        if (style.length() == 0)
            return;
        Text dummy = new Text();
        dummy.setStyle(editor.getStyleOfChar(index));
        String[] split = style.split(":");
        Styles.setProperty(dummy, split[0].trim(), split[1].trim());
        editor.setStyle(index, index + 1, dummy.getStyle());
    }

    private void restyleCharacter(int index, String style) {
        editor.setStyle(index, index + 1, style);
    }

    private void styleSelected(String s) {
        styleEdited = true;
        int start = selectedStart(), end = selectedEnd();
        for (int i = start; i < end; i++) {
            styleCharacter(i, s);
        }
    }

    private void styleSelected(String s, boolean on, String ifOn, String ifOff) {
        styleEdited = true;
        int start = selectedStart(), end = selectedEnd();
        for (int i = start; i < end; i++) {
            styleCharacter(i, s + ": " + (on ? ifOn : ifOff));
        }
    }

    private int selectedStart() {
        return editor.getSelection().getStart();
    }

    private int selectedEnd() {
        return editor.getSelection().getEnd();
    }

    private void submit() {
        AtomicBoolean checkConditions = new AtomicBoolean(true);
        Arrays.stream(submitConditions).forEach(condition-> checkConditions.set(checkConditions.get() && condition.getAsBoolean()));
        if (!checkConditions.get()) return;
        ((Pane) parent).getChildren().set(index, target);
        source.getChildren().clear();
        String currentStyle;
        for (int i = 0; i < editor.getLength(); i++) {
            Text current = new Text();
            String styleOfChar = editor.getStyleOfChar(i);
            currentStyle = styleOfChar;
            current.setStyle(styleOfChar);
            while (editor.getStyleOfChar(i).equals(currentStyle)) {
                current.setText(current.getText() + editor.getText().charAt(i));
                i++;
                if (i == editor.getLength())
                    break;
            }
            i--;
            source.getChildren().add(current);
        }
        User.active().getKeyMap().unlock();
        keyMap.lock();
        onFinished.accept(new CompressedRichText(source));
    }

    public StackPane getControls() {
        return controls;
    }

    public void setControls(StackPane controls) {
        this.controls = controls;
    }

    public InlineCssTextArea getEditor() {
        return editor;
    }

    public void setEditor(InlineCssTextArea editor) {
        this.editor = editor;
    }

    private class OperatorButton extends HBox {

        private boolean on;
        private final Text text;
        private boolean confirm;
        private boolean clicks;

        OperatorButton(String displayText, Font font, Consumer<Boolean> execute) {
            this(displayText, font, execute, true, false, null);
        }

        OperatorButton (String displayText, Font font, Consumer<Boolean> execute, boolean resetOnClick, boolean confirm, String confirmText) {
            setBorderColor(Color.TRANSPARENT);
            setPadding(Size.insets(8, 3));
            text = new Text(displayText);
            getChildren().add(text);
            text.setFont(font);
            this.confirm = confirm;
            clicks = false;
            addEventHandler(MouseEvent.MOUSE_ENTERED, event -> setBorderColor(Color.DARKGRAY));
            addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
                clicks = false;
                if (getChildren().size() > 1)
                    getChildren().remove(0);
                if (on)
                    setBorderColor(Color.BLACK);
                else setBorderColor(Color.TRANSPARENT);
            });
            addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (confirm && !clicks && (styleEdited || !exactText())) {
                    clicks = true;
                    Text text = new Text(confirmText);
                    text.setFont(Font.font(Size.fontSize(13)));
                    this.getChildren().add(0, text);
                    return;
                }
                if (getChildren().size() > 1)
                    getChildren().remove(0);
                if (resetOnClick) {
                    on = !on;
                    if (on)
                        setBorderColor(Color.BLACK);
                    else setBorderColor(Color.LIGHTGRAY);
                }
                execute.accept(on);
                editor.requestFocus();
            });
            setSpacing(Size.width(5));
            setAlignment(Pos.CENTER);
        }

        private void setBorderColor(Color c) {
            setBorder(new Border(new BorderStroke(c, BorderStrokeStyle.SOLID, new CornerRadii(Size.lessWidthHeight(10)), BorderStroke.DEFAULT_WIDTHS)));
        }

        void softEnable() {
            on = true;
            setBorderColor(Color.BLACK);
        }

        void softDisable() {
            on = false;
            setBorderColor(Color.TRANSPARENT);
        }

    }

    private boolean exactText() {
        String text = source.getChildren().stream().map(node -> {
            if (node instanceof Text) {
                return ((Text) node).getText();
            }
            return "";
        }).collect(Collectors.joining());
        return text.equals(editor.getText());
    }

    private static class AlignmentLines extends VBox {
        AlignmentLines(int pos) {
            Line[] lines = new Line[5];
            for (int i = 0; i < lines.length; i++) {
                lines[i] = new Line(0, i * Size.height(5), Size.width(pos == 4 ? 17 : i % 2 == 0 ? 15 : 8), i * Size.height(5));
                lines[i].setStrokeWidth(Size.height(2));
            }
            setAlignment(pos == 0 ? Pos.CENTER_LEFT : pos == 2 ? Pos.CENTER_RIGHT : Pos.CENTER);
            setSpacing(Size.height(3.5));
            getChildren().addAll(lines);
        }
    }

    private String currentStyle() {
        Text dummy = new Text();
        String family = fontSelector.getValue();
        if (family.length() == 0)
            family = "System";
        Styles.setProperty(dummy, "-fx-font-family", family);
        Styles.setProperty(dummy, "-fx-font-size", String.valueOf(fontSizeSelector.getValue()));
        if (bold.on)
            Styles.setProperty(dummy, "-fx-font-weight", "bold");
        if (italic.on)
            Styles.setProperty(dummy, "-fx-font-style", "italic");
        if (underline.on)
            Styles.setProperty(dummy, "-fx-underline", "true");
        if (strikethrough.on)
            Styles.setProperty(dummy, "-fx-strikethrough", "true");
        return dummy.getStyle();
    }

}
