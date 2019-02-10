package classes;

import java.util.ArrayList;

/**
 * Created by S507098 on 4/28/2017.
 */
public class Test extends HousedAssignment {

    private QuestionGroup allQuestions;
    private DisplayFormat displayFormat;
    private boolean availableMobile;

    public Test(String name, ArrayList<TestElement> elements) {
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
}
