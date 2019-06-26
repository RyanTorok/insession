package classes;

import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class QuestionUnit extends TestElement {

    private List<QuestionElement> subelements;

    public QuestionUnit(List<QuestionElement> subelements) {
        this.subelements = subelements;
    }

    public QuestionElement getElement(int n) {
        return subelements.get(n);
    }

    private double totalPoints() {
        double sum = 0;
        for (QuestionElement e : subelements) {
            if (e instanceof ValuedQuestionElement)
                sum += ((ValuedQuestionElement) e).getPointValue();
        }
        return sum;
    }

    public VBox build() {
        VBox main = new VBox();
        FlowPane current = new FlowPane();
        main.getChildren().add(current);
        for (QuestionElement e : subelements) {
            if (e instanceof QuestionText || e instanceof LineItemQuestion) {
                //add to current flow
                current.getChildren().add(e.getDisplay());
            } else {
                //go to newline, add element as its own, then create new flow
                current.getChildren().add(e.getDisplay());
                current = new FlowPane();
                main.getChildren().add(current);
            }
        }
        if (current.getChildren().isEmpty())
            //remove extraneous empty flowpane if there are no elements or the last one is non-inline
            main.getChildren().remove(current);
        return main;
    }

}
