package classes;

import java.util.ArrayList;

/**
 * Created by S507098 on 4/28/2017.
 */
public class Test extends HousedAssignment {

    public Test(String name, ArrayList<TestElement> elements) {
        super(name);
        overall = new QuestionGroup(elements);
    }
    private QuestionGroup overall;


}
