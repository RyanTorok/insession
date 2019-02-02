package classes;

import javafx.scene.control.TextField;

public class LineItemFieldPane extends TextField implements LineItemAnswerSupplier {
    @Override
    public LineItemAnswer getResponse() {
        return new LineItemAnswer(getText());
    }
}
