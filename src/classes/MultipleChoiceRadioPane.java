package classes;

import classes.MultipleChoiceQuestion.ChoiceDisplayType;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import main.Size;

import java.util.*;

public class MultipleChoiceRadioPane extends VBox implements MultipleChoiceAnswerSupplier {

    private final boolean scatter;
    private boolean multiple;
    private final String[] answers;
    private final ChoiceDisplayType displayType;

    private List<Element> elements;
    private char separator;

    public MultipleChoiceRadioPane(boolean scatter, boolean multiple, ChoiceDisplayType displayType, String... answers) {
        this.scatter = scatter;
        this.multiple = multiple;
        this.answers = answers;
        this.displayType = displayType;
        separator = '.';

        boolean dontShuffle = false;
        ObservableList<Node> children = getChildren();
        for (int i = 0, answersLength = answers.length; i < answersLength; i++) {
            String ans = answers[i];
            //don't shuffle if answers like "Both A and B are correct" or "III and IV are both correct" are options.
            dontShuffle = dontShuffle || scatter && ans.toLowerCase().trim().matches("(both\\s+)?+.+?\\s+and\\s+.+?.*");
            children.add(new Element(multiple, displayType, i, i, ans));
        }
        if (scatter && !dontShuffle) {
            Collections.shuffle(children);
            //move answers like "All of the above" and "None of these" to the bottom.
            children.sort(Comparator.comparing(c -> matchesReflective(((Element) c).origAnswerText())));
            for (int i = 0, childrenSize = children.size(); i < childrenSize; i++) {
                Node n = children.get(i);
                if (n instanceof Element) {
                    ((Element) n).setDisplayIndex(i);
                }
            }
        }
        setPadding(Size.insets(10));

    }

    private boolean matchesReflective(String ans) {
        return ans.toLowerCase().trim().matches("(all|none)\\s+of\\s+(these|those|the).*");
    }

    private class Element extends HBox {
        boolean which; //true for checkbox, false for radio button
        CheckBox checkBox;
        RadioButton radioButton;
        private int index;
        private int displayIndex;
        String answerText;

        public Element(boolean which, ChoiceDisplayType displayType, int index, int displayIndex, String answerText) {
            this.which = which;
            this.index = index;
            this.displayIndex = displayIndex;
            this.answerText = answerText;
            String text = MultipleChoiceQuestion.getAnswerText(displayType, displayIndex, answerText, separator);
            if (which) checkBox = new CheckBox(text);
            else radioButton = new RadioButton(text);
        }

        boolean isSelected() {
            if (which)
                return checkBox.isSelected();
            else return radioButton.isSelected();
        }


        private String origAnswerText() {
            return answerText;
        }

        void setDisplayIndex(int displayIndex) {
            this.displayIndex = displayIndex;
            String text = MultipleChoiceQuestion.getAnswerText(displayType, displayIndex, answerText, separator);
            if (which) checkBox.setText(text);
            else radioButton.setText(text);
        }

    }

    @Override
    public MultipleChoiceAnswer getResponse() {
        TreeSet<Integer> pickedAnswers = new TreeSet<>();
        ObservableList<Node> children = getChildren();
        for (Node n : children) {
            if (n instanceof Element) {
                Element element = (Element) n;
                if (element.isSelected())
                    pickedAnswers.add(element.index);
            }
        }
        return new MultipleChoiceAnswer(pickedAnswers);
    }

    public void setSeparator(char separator) {
        this.separator = separator;
    }



}
