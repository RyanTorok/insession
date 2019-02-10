package classes;

import javafx.scene.Node;

import java.util.Collection;
import java.util.List;

public class MultipleChoiceQuestion extends QuestionElement {

    private List<MultipleChoiceAnswer> correctAnswers;
    private boolean scatterChoices;
    private boolean allowMultipleSelections;
    private boolean partialCredit;
    private ChoiceDisplayType displayType;
    private List<String> answerChoices;
    private boolean pullDown;


    public MultipleChoiceQuestion(List<MultipleChoiceAnswer> answers) {
        this.correctAnswers = answers;
    }

    @Override
    protected Collection<MultipleChoiceAnswer> getCorrectResponses() {
        return correctAnswers;
    }

    @Override
    public void addCorrectAnswer(Answer answer) {
        if (!(answer instanceof MultipleChoiceAnswer))
            throw new IllegalArgumentException("non-MC answer added for MC question");
        correctAnswers.add((MultipleChoiceAnswer) answer);
    }

    @Override
    protected Node getAnswerSpaceDisplay() {
        if (pullDown) return new MultipleChoicePullDownPane(displayType, answerChoices.toArray(new String[0]));
        else return new MultipleChoiceRadioPane(scatterChoices, allowMultipleSelections, displayType, answerChoices.toArray(new String[0]));
    }

    public List<String> getAnswerChoices() {
        return answerChoices;
    }

    public enum ChoiceDisplayType {
        UPPERCASE, LOWERCASE, NUMBER, UPPER_ROMAN, LOWER_ROMAN, NONE
    }

    static String getAnswerText(ChoiceDisplayType displayType, int index, String answerText, char separator) {
        String lead;
        switch (displayType) {
            case UPPERCASE:
                lead = getLetter(index).toUpperCase();
                break;
            case LOWERCASE:
                lead = getLetter(index);
                break;
            case NUMBER:
                lead = String.valueOf(index);
                break;
            case UPPER_ROMAN:
                lead = RomanNumber.toRoman(index);
                break;
            case LOWER_ROMAN:
                lead = RomanNumber.toRoman(index).toLowerCase();
                break;
            case NONE:
                return answerText;
            default:
                throw new IllegalStateException("no selected answer format branch");
        }
        return lead + separator + " " + answerText;
    }

    private static String getLetter(int index) {
        char letter = (char) ((index % 26) + 'a');
        int iterations = index / 26;
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i <= iterations; i++) {
            letters.append(letter);
        }
        return letters.toString();
    }
}
