package classes;

import java.util.ArrayList;

/**
 * Created by S507098 on 4/28/2017.
 */
public class QuestionGroup extends TestElement {

    private ArrayList<TestElement> subelements;
    private QuestionObject groupwideDisplayObj;
    private boolean returnable;
    private boolean scatterQuestions;
    private TestTimingRule sectiontimer;

    public QuestionGroup(ArrayList<TestElement> elements) {
        subelements = elements;
        sectiontimer = new TestTimingRule();
    }

    public TestTimingRule getSectiontimer() {
        return sectiontimer;
    }

    public void setSectiontimer(TestTimingRule sectiontimer) {
        this.sectiontimer = sectiontimer;
    }

    public ArrayList<TestElement> getSubelements() {
        return subelements;
    }

    public void addSubElement(int index, TestElement e) {
        subelements.add(index, e);
    }

    public void addSubElement(TestElement e) {
        subelements.add(e);
    }

    public boolean removeSubElement(TestElement e) {
        return subelements.remove(e);
    }

    public TestElement removeSubElementAtIndex(int index) {
        return subelements.remove(index);
    }

}
