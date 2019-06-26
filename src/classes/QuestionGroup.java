package classes;

import java.util.ArrayList;

/**
 * Created by S507098 on 4/28/2017.
 */
public class QuestionGroup extends TestElement {

    private ArrayList<QuestionUnit> subelements;
    private QuestionObject groupwideDisplayObj;
    private boolean returnable;
    private boolean scatterQuestions;
    private TestTimingRule sectiontimer;

    public QuestionGroup(ArrayList<QuestionUnit> elements) {
        subelements = elements;
        sectiontimer = new TestTimingRule();
    }

    public TestTimingRule getSectiontimer() {
        return sectiontimer;
    }

    public void setSectiontimer(TestTimingRule sectiontimer) {
        this.sectiontimer = sectiontimer;
    }

    public ArrayList<QuestionUnit> getSubelements() {
        return subelements;
    }

    public void addSubElement(int index, QuestionUnit e) {
        subelements.add(index, e);
    }

    public void addSubElement(QuestionUnit e) {
        subelements.add(e);
    }

    public boolean removeSubElement(QuestionUnit e) {
        return subelements.remove(e);
    }

    public QuestionUnit removeSubElementAtIndex(int index) {
        return subelements.remove(index);
    }

    public QuestionUnit get(int n) {
        return subelements.get(n);
    }


    public int size() {
        return subelements.size();
    }
}
