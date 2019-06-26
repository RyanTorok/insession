package classes;

import javafx.scene.Node;

import java.util.Collection;

/**
 * Created by S507098 on 4/28/2017.
 */
public abstract class ValuedQuestionElement extends QuestionElement {
    private int pointValue;

    protected abstract Collection<? extends Answer> getCorrectResponses();
    public boolean verify(Answer response) {
        for (Answer ans : getCorrectResponses()) {
            if (response.verify(ans))
                return true;
        }
        return false;
    }

    public abstract void addCorrectAnswer(Answer answer);

    protected abstract Node getDisplay();

    public int getPointValue() {
        return pointValue;
    }

    public void setPointValue(int pointValue) {
        this.pointValue = pointValue;
    }
}
