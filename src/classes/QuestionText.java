package classes;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class QuestionText extends QuestionElement {

    private String text;
    private Text displayText;

    public QuestionText(String text) {
        this.setText(text);
    }

    @Override
    protected Node getDisplay() {
        displayText = new Text(this.getText());
        displayText.setFill(Color.BLACK);
        return new TextFlow(displayText);
    }

    void setFont(Font f) {
        displayText.setFont(f);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
