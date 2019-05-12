package main;

import javafx.scene.text.Text;

public class TruncatedText extends Text {
    private String fullText;
    private int maxChars;
    private int wordEndTolerance;
    private boolean lineUpEllipsis;


    public TruncatedText(String s, int maxChars) {
        this(s, maxChars, 0, false);
    }

    public TruncatedText(String s, int maxChars, int wordEndTolerance, boolean lineUpEllipsis) {
        fullText = s;
        setText(truncate(s, maxChars, wordEndTolerance, lineUpEllipsis));
    }

    public void retruncate(int maxChars, int wordEndTolerance, boolean lineUpEllipsis) {
        setText(truncate(fullText, maxChars, wordEndTolerance, lineUpEllipsis));
    }

    //returns a truncated version of the given string to the given number of characters, with a "..." included if necessary.
    private String truncate(String s, int maxChars, int wordEndTolerance, boolean lineUpEllipsis) {
        this.maxChars = maxChars;
        this.wordEndTolerance = wordEndTolerance;
        this.lineUpEllipsis = lineUpEllipsis;
        if (s.length() <= maxChars)
            return s;
        if (maxChars < 4)
            return "...".substring(0, maxChars);
        maxChars -= 3; // we need space for the "..."

        /*
            If we manage to find the end of a word, we include its ending space,
            but if we don't, we put the "..." with no space after the ending word segment.
        */

        for (int i = maxChars; i >= maxChars - wordEndTolerance; i--) {
            if (Character.toString(s.charAt(i)).matches("\\s")) {
                StringBuilder gap = new StringBuilder();
                if (lineUpEllipsis) {
                    for (int j = 0; j < maxChars - i; j++) {
                        gap.append(" ");
                    }
                }
                return s.substring(0, i + 1) + gap.toString() + "...";
            }
        }
        return s.substring(0, maxChars) + "...";
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
        setText(truncate(fullText, maxChars, wordEndTolerance, lineUpEllipsis));
    }

    public String getFullText() {
        return fullText;
    }

    public void expand() {
        setText(fullText);
    }

    public void collapse() {
        setText(truncate(fullText, maxChars, wordEndTolerance, lineUpEllipsis));
    }
}

