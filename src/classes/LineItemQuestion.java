package classes;

import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import main.Size;

import java.util.Collection;
import java.util.Set;

public class LineItemQuestion extends ValuedQuestionElement {
    private Set<LineItemAnswer> correctAnswers;
    private boolean caseSensitive = false;
    private boolean enableSpellCheck = true;      //User level spell checker
    private boolean enableSpellCorrection = true; //Automatic correction of spelling errors when answer is checked
    private boolean ignoreNonAlphanumeric = true;

    @Override
    protected Collection<LineItemAnswer> getCorrectResponses() {
        return getCorrectAnswers();
    }

    @Override
    public void addCorrectAnswer(Answer answer) {
        if (answer instanceof LineItemAnswer)
            getCorrectAnswers().add((LineItemAnswer) answer);
        else throw new IllegalArgumentException("unmatched answer type for line item question");
    }

    @Override
    protected Pane getDisplay() {
        TextField answerBlank = new TextField();
        answerBlank.setPrefColumnCount(60);
        answerBlank.setPadding(Size.insets(10));
        answerBlank.setFont(Font.font("Serif", Size.fontSize(20)));
        return new HBox(answerBlank);
    }

    public Set<LineItemAnswer> getCorrectAnswers() {
        return correctAnswers;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isEnableSpellCheck() {
        return enableSpellCheck;
    }

    public void setEnableSpellCheck(boolean enableSpellCheck) {
        this.enableSpellCheck = enableSpellCheck;
    }

    public boolean isIgnoreNonAlphanumeric() {
        return ignoreNonAlphanumeric;
    }

    public void setIgnoreNonAlphanumeric(boolean ignoreNonAlphanumeric) {
        this.ignoreNonAlphanumeric = ignoreNonAlphanumeric;
    }

    public boolean isEnableSpellCorrection() {
        return enableSpellCorrection;
    }

    public void setEnableSpellCorrection(boolean enableSpellCorrection) {
        this.enableSpellCorrection = enableSpellCorrection;
    }

}
