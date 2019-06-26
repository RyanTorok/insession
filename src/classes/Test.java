package classes;

import gui.ArrowFlow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

/**
 * Created by S507098 on 4/28/2017.
 */
public class Test extends HousedAssignment {

    private QuestionGroup allQuestions;
    private DisplayFormat displayFormat;
    private boolean availableMobile;
    private TestRandomization order;

    public Test(String name, ArrayList<QuestionUnit> elements) {
        super(name);
        allQuestions = new QuestionGroup(elements);
        displayFormat = DisplayFormat.LIST_ALL;
        availableMobile = true;
    }

    public boolean isAvailableMobile() {
        return availableMobile;
    }

    public void setAvailableMobile(boolean availableMobile) {
        this.availableMobile = availableMobile;
    }

    public DisplayFormat getDisplayFormat() {
        return displayFormat;
    }

    public void setDisplayFormat(DisplayFormat displayFormat) {
        this.displayFormat = displayFormat;
    }

    public QuestionGroup getQuestions() {
        return allQuestions;
    }

    enum DisplayFormat {
        LIST_ALL, LIST_GROUP, INDIVIDUAL
    }

    public void addQuestion(QuestionUnit e) {
        allQuestions.addSubElement(e);
    }

    public QuestionUnit getQuestion(int n) {
        return allQuestions.get(order.getOriginal(n));
    }

    public Pane makeDisplay() {
        ArrayList<VBox> questionPanes = new ArrayList<>();
        for (int i = 0; i < allQuestions.size(); i++) {
            questionPanes.add(getQuestion(i).build());
        }
        if (displayFormat == DisplayFormat.LIST_ALL) {
            VBox build = new VBox();
            build.getChildren().addAll(questionPanes);
            return build;
        }
        if (displayFormat == DisplayFormat.LIST_GROUP) {
            return new ArrowFlow(questionPanes, ArrowFlow.HORIZONTAL);
        }
        return null;
    }

}
