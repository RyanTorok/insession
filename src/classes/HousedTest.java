package classes;

import java.util.ArrayList;

/**
 * Created by 11ryt on 5/24/2017.
 */
public class HousedTest {
    private Test original;
    private Test makeup;
    private ArrayList<Test> retakes;
    private Expression retakepolicy;

    public HousedTest(Test original){
        retakes = new ArrayList();
        this.original = original;
        this.makeup = original;
        retakes.add(original);
    }

}
