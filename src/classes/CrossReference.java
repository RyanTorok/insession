package classes;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import main.Events;

public class CrossReference extends QuestionText {
    private QuestionUnit target;

    public CrossReference(String text, QuestionUnit target) {
        super(text);
        this.target = target;
        if (getDisplay() instanceof Text) {
            ((Text) getDisplay()).setUnderline(true);
        }
        Events.highlightOnMouseOver(getDisplay(), Color.BLUE);
    }

}
