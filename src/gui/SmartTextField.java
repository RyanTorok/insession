package gui;

import javafx.scene.control.TextField;
import javafx.util.Pair;

import java.util.List;

public class SmartTextField extends TextField {

    private List<Pair<Integer, Integer>> actualText;

    public SmartTextField(String s) {
        super(s);
    }

    String getTypedText() {
        String orig = getText();
        StringBuilder text = new StringBuilder();
        for (Pair<Integer, Integer> p : actualText) {
            text.append(orig.substring(p.getKey(), p.getValue()));
        }
        return text.toString();
    }

    void edit(int index, int length, String oldText, String newText, boolean user) {

    }

    protected static class Rule {
        String regex;
        List<EditConstraint> textToModify;

        void apply(SmartTextField field) {
            if (field.getTypedText().matches(regex)) {
                for (EditConstraint e : textToModify){
                    int index = e.getIndex();
                    if (e.insert) {
                        field.edit(index, e.getLength(), field.getTypedText(), e.getReplace(), false);
                    } else {
                        field.edit(index, e.getLength(), field.getTypedText(), "", false);
                    }
                }
            }
        }

    }

    private static class EditConstraint {
        private boolean insert;
        private int index;
        private int length;
        private String replace;

        public int getIndex() {
            return index;
        }

        public boolean isInsert() {
            return insert;
        }

        public void setInsert(boolean insert) {
            this.insert = insert;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public String getReplace() {
            return replace;
        }

        public void setReplace(String replace) {
            this.replace = replace;
        }
    }
}
