package gui;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import main.Colors;
import main.Size;
import terminal.Address;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HTMLText {
    private String source;
    private String unformatted;
    private TextFlow formatted;

    public HTMLText(String source) {
        this.source = source;
        formatted = format(source);
        unformatted = formatted.getChildren().stream().map(text -> ((Text) text).getText()).collect(Collectors.joining());
    }
    
    private TextFlow format(String source) {    
        TextFlow textFlow = new TextFlow();
        textFlow.getChildren().addAll(parseHTML(source));
        return textFlow;
    }

    private int i = 0;

    private List<Text> parseHTML(String source) {
        ArrayList<Text> elements = new ArrayList<>();
        ArrayList<String> activeTags = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < source.length(); i++) {
            switch (source.charAt(i)) {
                case '<': {
                    //handle tag
                    i++;
                    boolean close = source.charAt(i) == '/';
                    if (close) i++;
                    int end = source.indexOf('>', i);
                    String tag = source.substring(i, end);
                    Text text = new Text(current.toString());
                    formatBlock(text, activeTags);
                    elements.add(text);
                    if (close) {
                        for (int j = activeTags.size() - 1; j >= 0; j--) {
                            if (activeTags.get(j).equals(tag) || activeTags.get(j).startsWith(tag + " ")) {
                                activeTags.remove(j);
                                break;
                            }
                        }
                    } else {
                        activeTags.add(tag);
                    }
                    current = new StringBuilder();
                    i = end;
                }
                break;
                case '&':
                    int endIndex = source.indexOf(";", i + 1);
                    if (endIndex == -1)
                        throw new IllegalArgumentException("Unterminated escape sequence at character index " + i);
                    String escapeSeq = source.substring(i + 1, endIndex);
                    i = endIndex;
                    switch (escapeSeq) {
                        case "amp":
                            current.append('&');
                            break;
                        case "lt":
                            current.append('<');
                            break;
                        case "gt":
                            current.append('>');
                            break;
                        case "quot":
                            current.append('\"');
                            break;
                        case "#39":
                            current.append('\'');
                            break;
                    }
                    break;
                default:
                    current.append(source.charAt(i));
            }
        }
        return elements;
    }

    private void formatBlock(Text text, ArrayList<String> activeTags) {
        for (String tag : activeTags) {
            int index = tag.indexOf(" ");
            String name = index == -1 ? tag : tag.substring(0, index);
            switch (name) {
                case "b":
                    text.setFont(Font.font(text.getFont().getFamily(), FontWeight.BOLD, text.getFont().getSize()));
                    break;
                case "i":
                    text.setFont(Font.font(text.getFont().getFamily(), FontPosture.ITALIC, text.getFont().getSize()));
                    break;
                default:
                    break;
            }
            index = tag.indexOf("style=\"") + 7;
            if (index == 6) //i.e. nonexistent -1 + 7 = 6
                return;
            System.out.println(tag + " " + index);
            String style = tag.substring(index, tag.indexOf("\"", index));
            String[] split = style.split(";");
            for (String component : split) {
                parseStyleComponent(text, component);
            }
        }
    }

    private void parseStyleComponent(Text text, String component) {
        component = component.trim();
        if (component.length() == 0)
            return;
        String[] split = component.split(":");
        split[1] = split[1].trim();
        switch (split[0].trim()) {
            case "font-family":
                text.setFont(Font.font(split[1]));
                break;
            case "font-size":
                //must be in format 'font-size: XXX.XXXXXpt
                text.setFont(Font.font(Size.fontSize(Double.parseDouble(split[1].substring(0, split[1].length() - 2)))));
                break;
            case "color":
                text.setFill(Color.web(split[1]));
                break;
            default:
                break;
        }
    }

    public File generateHTML() {
        String filename = String.valueOf(((Long) System.nanoTime()).hashCode());
        //write file for load
        File file = new File(Address.fromRootAddr("webView", filename + ".html"));
        while (file.exists()) {
            filename = filename + "_2";
            file = new File(filename);
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(getSource());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }


    public static HTMLText makeHTML(TextFlow source) {
        StringBuilder out = new StringBuilder();
        out.append("<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js\"></script>");
        out.append("<script type=\"text/javascript\" src=\"inlineTextEditor.js\"></script>");
        out.append("<div>");
        for (Node n : source.getChildren()) {
            if (n instanceof Text) {
                Text t = (Text) n;
                out.append("<p contenteditable=\"true\" style=\"outline: none ; display: inline ; color: ");
                out.append(Colors.colorToHex(t.getFill() instanceof Color ? ((Color) t.getFill()) : Color.BLACK));
                out.append(" ; ");
                out.append("font-family: " + t.getFont().getFamily() + " ; ");
                out.append("font-style: " + t.getFont().getStyle() + " ; ");
                out.append("font-size: " + Size.fontSize(t.getFont().getSize()) + "pt ; ");
                out.append("\">");
                out.append(((Text) n).getText());
                out.append("</p>");
            }
        }
        out.append("</div>");
        System.out.println("HTML produced: " + out.toString());
        return new HTMLText(out.toString());
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
