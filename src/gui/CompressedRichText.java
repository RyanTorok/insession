package gui;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import org.json.JSONObject;

import java.io.StringBufferInputStream;
import java.util.*;

public class CompressedRichText {
    private String unformattedText;
    private HashMap<String, List<Pair<Integer, Integer>>> styles;

    public CompressedRichText(TextFlow flow) {
        unformattedText = "";
        styles = new HashMap<>();
        int characterIndex = 0;
        for (Node n : flow.getChildren()) {
            if (n instanceof Text) {
                Text t = (Text) n;
                String[] myStyles = t.getStyle().split(";");
                int textLength = t.getText().length();
                for (String component : myStyles) {
                    if (component.trim().length() == 0)
                        continue;
                    List<Pair<Integer, Integer>> mappings = styles.computeIfAbsent(component, k -> new ArrayList<>());
                    mappings.add(new Pair<>(characterIndex, characterIndex + textLength));
                }
                characterIndex += textLength;
                unformattedText += t.getText();
            }
        }
    }

    public TextFlow extract() {
        return extract(null);
    }

    public TextFlow extract(Region bindBackground) {
        //TODO optimize this. Stress tests cause havoc since we have every character as a different Label node

        TextFlow flow;
        if (bindBackground == null)
            flow = new TextFlow();
        else
            flow = new AutoColoredTextFlow(bindBackground);

        for (int i = 0; i < unformattedText.length(); i++) {
                flow.getChildren().add(new Text(Character.toString(unformattedText.charAt(i))));
        }
        for (String styleComponent : styles.keySet()) {
            List<Pair<Integer, Integer>> indexList = styles.get(styleComponent);
            for (Pair<Integer, Integer> range : indexList) {
                for (int i = range.getKey(); i < range.getValue(); i++) {
                    String[] split = styleComponent.split(":");
                    try {
                        Styles.setProperty(flow.getChildren().get(i), split[0], split[1]);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("error!");
                    }
                }
            }
        }
        return flow;
    }

    public String getUnformattedText() {
        return unformattedText;
    }

    @Override
    public String toString() {
        return unformattedText;
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.append("text", unformattedText);
        jsonObject.append("format", getStyleRegex());
        return jsonObject;
    }

    public String getStyleRegex() {
        StringBuilder regex = new StringBuilder();
        for (String s : styles.keySet()) {
               regex.append(s);
               regex.append(" ( ");
            for (Pair<Integer, Integer> p : styles.get(s)) {
                regex.append(p.getKey()).append(" ").append(p.getValue()).append(" ");
            }
            regex.append(") ");
        }
        return regex.toString().trim();
    }

    public void setStyleFromRegex(String s) throws IllegalArgumentException {
        styles.clear();
        for (int i = 0; i < s.length(); i++) {
            String styleCode = strNext(s, i);
            i += styleCode.length() + 1;
            if (!strNext(s, i).equals("(")) throw new IllegalArgumentException("illegal style regex format");
            i += 2;
            String num1 = strNext(s, i);
            List<Pair<Integer, Integer>> styleList = new ArrayList<>();
            while (!num1.equals(")")) {
                num1 = strNext(s, i);
                i += num1.length() + 1;
                String num2 = strNext(s, i);
                i += num2.length() + 1;
                try {
                    Integer start = Integer.parseInt(num1), end = Integer.parseInt(num2);
                    if (start > end)
                        throw new IllegalArgumentException("illegal style regex format: end before start");
                    styleList.add(new Pair<>(start, end));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("illegal style regex format", e);
                }
            }
            styles.put(styleCode, styleList);
        }
    }

    private String strNext(String s, int index) {
        int start = index;
        while (index < s.length() && s.charAt(index) != ' ')
            index++;
        return s.substring(start, index);
    }
}
