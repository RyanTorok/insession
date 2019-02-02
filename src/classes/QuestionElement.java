package classes;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.Collection;

/**
 * Created by S507098 on 4/28/2017.
 */
public abstract class QuestionElement extends TestElement {
    private int points;

    protected abstract Collection<? extends Answer> getCorrectResponses();
    public boolean verify(Answer response) {
        for (Answer ans : getCorrectResponses()) {
            if (response.verify(ans))
                return true;
        }
        return false;
    }

    public abstract void addCorrectAnswer(Answer answer);

    protected abstract Node getAnswerSpaceDisplay();
}
