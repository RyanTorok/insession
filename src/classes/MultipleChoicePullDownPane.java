package classes;

import javafx.scene.control.ComboBox;

import java.util.Collections;
import java.util.TreeMap;

public class MultipleChoicePullDownPane extends ComboBox<String> implements MultipleChoiceAnswerSupplier {
    private char separator = '.';
    private TreeMap<String, Integer> keys;

    MultipleChoicePullDownPane(MultipleChoiceQuestion.ChoiceDisplayType displayType, String[] strings) {
        keys = new TreeMap<>();
        for (int i = 0, stringsLength = strings.length; i < stringsLength; i++) {
            String ans = strings[i];
            String answerText = MultipleChoiceQuestion.getAnswerText(displayType, i, ans, separator);
            keys.put(answerText, i);
            getItems().add(answerText);
        }
    }

    public void setSeparator(char separator) {
        this.separator = separator;
    }

    @Override
    public MultipleChoiceAnswer getResponse() {
        return new MultipleChoiceAnswer(Collections.singleton(keys.get(getValue())));
    }
}