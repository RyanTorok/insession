package gui;

import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RichText {
    private String source;
    private String unformatted;
    private TextFlow formatted;

    public RichText(String source) {
        this.source = source;
        formatted = format(source);
        unformatted = formatted.getChildren().stream().map(text -> ((Text) text).getText()).collect(Collectors.joining());
    }

    private TextFlow format(String source) {
        return new TextFlow() {{
            getChildren().addAll(singleton(source));
        }};
    }

    private int i = 0;

    private List<Text> singleton(String source) {
        ArrayList<Text> elements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escape = false;
        for (; i < source.length(); i++) {
            if (escape) {
                current.append(source.charAt(i));
                escape = false;
                continue;
            }
            switch (source.charAt(i)) {
                case '\\':
                    escape = true;
                    break;
                case '%':
                    elements.add(new Text(current.toString()));
                    int endIndex = source.indexOf("{", i);
                    String cmd = source.substring(i + 1, endIndex);
                    i = endIndex + 1;
                    List<Text> sublet = singleton(source);
                    sublet.forEach(text1 -> applyTag(text1, cmd));
                case '}':
                    elements.add(new Text(current.toString()));
                    return elements;

                default:
                    current.append(source.charAt(i));
            }
        }
        //end of string
        elements.add(new Text(current.toString()));
        return elements;
    }

    private void applyTag(Text text, String cmd) {
        System.out.println("apply tag '" + cmd + "' to the text: " + text.getText());
        Font orig = text.getFont();
        if (cmd.matches("color=#.......\\..")) {
            int red = Integer.parseInt(cmd.substring(7, 9), 16);
            int green = Integer.parseInt(cmd.substring(9, 11), 16);
            int blue = Integer.parseInt(cmd.substring(11, 13), 16);
            double opacity = Double.parseDouble(cmd.substring(13));
            text.setFill(new Color(red / (double) 255, green / (double) 255, blue / (double) 255, opacity));
            return;
        }
        switch (cmd.toLowerCase()) {
            case "bold":
                text.setFont(Font.font(orig.getFamily(), FontWeight.BOLD, orig.getSize()));
                break;
            case "italic":
                text.setFont(Font.font(orig.getFamily(), FontPosture.ITALIC, orig.getSize()));
                break;
            case "underline":
                text.setUnderline(true);
                break;
            case "superscript":
//                    text.setFont(Font.font(orig.getFamily(), );
        }
    }

    public String getUnformatted() {
        return unformatted;
    }

    public void setUnformatted(String unformatted) {
        this.unformatted = unformatted;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public TextFlow asTextFlow() {
        return formatted;
    }

    public void setFormatted(TextFlow formatted) {
        this.formatted = formatted;
    }
}
