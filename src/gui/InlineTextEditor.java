package gui;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.*;
import main.Colors;
import main.Events;
import main.Size;
import main.User;
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
        ((Pane) parent).getChildren().set(index, new InlineTextEditor(target, source, parent, index, onFinished, submitConditions));
        User.active().getKeyMap().lock();
    }


    public InlineTextEditor(Node target, TextFlow source, Parent parent, int index, Consumer<CompressedRichText> onFinished, BooleanSupplier... submitConditions) {
        super();
        this.target = target;
        this.source = source;
        this.parent = parent;
        this.index = index;
        this.onFinished = onFinished;
        this.submitConditions = submitConditions;

        Styles.setBackgroundColor(this, Color.LIGHTGRAY);
        setPadding(Size.insets(20));
        setSpacing(Size.height(10));

        HBox textControls = new HBox();
        Styles.setBackgroundColor(textControls, Color.LIGHTGRAY);
        textControls.setPadding(Size.insets(15));

        double fontSize = Size.fontSize(24);

        ChoiceBox<String> fontSelector = new ChoiceBox<>(FXCollections.observableArrayList(Font.getFamilies()));
        fontSelector.setValue(Font.getDefault().getFamily());
        fontSelector.setMaxHeight(Size.height(500));
        fontSelector.valueProperty().addListener((observable, oldValue, newValue) -> styleSelected("-fx-font-family : " + newValue));

        Spinner<Double> fontSizeSelector = new Spinner<>(new SpinnerValueFactory<Double>() {
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
        OperatorButton bold = new OperatorButton("B", Font.font("DejaVu Sans Mono", FontWeight.BOLD, fontSize), (on)-> styleSelected("-fx-font-weight", on, "bold", "normal"));

        //italic button
        OperatorButton italic = new OperatorButton("I", Font.font("DejaVu Sans Mono", FontPosture.ITALIC, fontSize),(on)-> styleSelected("-fx-font-style", on, "italic", "normal"));

        //underline button
        OperatorButton underline = new OperatorButton("U", Font.font("DejaVu Sans Mono", fontSize),(on)-> styleSelected("-fx-underline", on, "true", "false"));
        underline.text.setUnderline(true);

        //strikethrough button
        OperatorButton strikethrough = new OperatorButton("S", Font.font("DejaVu Sans Mono", fontSize), (on)-> styleSelected("-fx-strikethrough", on, "true", "false"));
        strikethrough.text.setStrikethrough(true);

        //symbol button
        OperatorButton symbol = new OperatorButton(Character.toString((char) 0x3a9), Font.font("DejaVu Sans Mono", fontSize), (on)-> {

        });

        //math equation button
        OperatorButton math = new OperatorButton(Character.toString((char) 0xf7), Font.font("DejaVu Sans Mono", fontSize), (on)-> {

        });

        var alignments = new Object() {
            OperatorButton leftAlign = null;
            OperatorButton centerAlign = null;
            OperatorButton rightAlign = null;
            OperatorButton justifyAlign = null;
        };

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
        OperatorButton doubleSpace = new OperatorButton("", Font.font("DejaVu Sans Mono", fontSize), (on)-> {

        });

        //text color chooser
        ColorPicker textColor = new ColorPicker();
        textColor.valueProperty().addListener((observable, oldValue, newValue) -> styleSelected("-fx-text-inner-color: " + Colors.colorToHex(newValue).toLowerCase()));

        //highlight color chooser
        ColorPicker highlightColor = new ColorPicker();
        highlightColor.valueProperty().addListener((observable, oldValue, newValue) -> {
            styleSelected("-fx-highlight-fill : " + Colors.colorToHex(newValue).toLowerCase());
            System.out.println(editor.getStyleAtPosition(selectedStart()));
        });


        //submit button
        OperatorButton submit = new OperatorButton("Submit", Font.font(fontSize), (on)-> submit());

        HBox mainControls = new HBox(fontSelector, fontSizeSelector, bold, italic, underline, strikethrough, symbol, math, alignments.leftAlign, alignments.centerAlign, alignments.rightAlign, alignments.justifyAlign, doubleSpace, textColor, highlightColor, submit);
        mainControls.setSpacing(Size.width(5));
        controls = new StackPane(mainControls);
        Styles.setBackgroundColor(controls, Color.LIGHTGRAY);

        editor = new InlineCssTextArea(source.getChildren().stream().map(text -> {
            if (text instanceof Text)
                return ((Text) text).getText();
            else return "";
        }).collect(Collectors.joining()));

        AtomicInteger counter = new AtomicInteger(0);
        source.getChildren().forEach(node -> {
            if (node instanceof Text) {
               Text text = (Text) node;
                int length = text.getText().length();
                for (int i = 0; i < length; i++) {
                    styleCharacter(counter.get() + i, text.getStyle());
                }
                counter.getAndAdd(length);
            }
        });

        editor.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
                User.active().getKeyMap().lock();
            else User.active().getKeyMap().unlock();
        });
        editor.caretPositionProperty().addListener((observable, oldValue, newValue) -> {
            int indexToCheck = editor.getSelection().getLength() > 0 ? selectedStart() : Math.max(0, newValue);
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
            if (alignment != null) {
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
            if (fontSz == "")
                fontSz = "12.0";
            if (fontSz.endsWith("pt") || fontSz.endsWith("px") || fontSz.endsWith("em"))
                fontSz = fontSz.substring(0, fontSz.length() - 2);

            for (int i = selectedStart(); i < selectedEnd(); i++) {
                Text dummy1 = new Text();
                dummy1.setStyle(editor.getStyleOfChar(i));
                if (!(fontFamily.equals(Styles.getProperty(dummy1, "-fx-font-family")))) allSameFont = false;
                if (!(fontSz.equals(Styles.getProperty(dummy1, "-fx-font-size")))) allSameSize = false;
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

    private void styleSelectedParagraph(String s) {
        for (int i = 0; i < editor.getParagraphs().size(); i++) {
            IndexRange selectedRange = editor.getParagraphSelection(i);
            if (selectedRange.getLength() > 0)
                editor.setStyle(i, s);
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

    private void styleSelected(String s) {
        int start = selectedStart(), end = selectedEnd();
        for (int i = start; i < end; i++) {
            styleCharacter(i, s);
        }
    }

    private void styleSelected(String s, boolean on, String ifOn, String ifOff) {
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

    private static class OperatorButton extends HBox {

        private boolean on;
        private final Text text;

        OperatorButton (String displayText, Font font, Consumer<Boolean> execute) {
            setBorderColor(Color.TRANSPARENT);
            setPadding(Size.insets(8, 3));
            text = new Text(displayText);
            getChildren().add(text);
            text.setFont(font);
            addEventHandler(MouseEvent.MOUSE_ENTERED, event -> setBorderColor(Color.DARKGRAY));
            addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
                if (on)
                    setBorderColor(Color.BLACK);
                else setBorderColor(Color.TRANSPARENT);
            });
            addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                on = !on;
                if (on)
                    setBorderColor(Color.BLACK);
                else setBorderColor(Color.LIGHTGRAY);
                execute.accept(on);
            });
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
}
