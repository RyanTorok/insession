package classes.assignments;

import classes.AbstractFunction;
import classes.NumberListOperation;
import main.User;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GradeSchema {

    /*   (Inclusive) bounds check applied to pre-curved grades, just intended to prevent typos.
         If the curve is written incorrectly, you're on your own.
    */
    private double[] acceptableRange;
    /*
        Actual point total this assignment is graded out of (i.e. if this is 80, then a grade of 40 would represent 50%).
     */
    private double pointsPossible;
    private NumberListOperation curve;
    private AbstractFunction<List<Double>, Double> lateRule;
    private AbstractFunction<Double, GradeMnemonic> mnemonicAssignment;

    public GradeSchema(double[] acceptableRange, double pointsPossible, NumberListOperation curve, AbstractFunction<List<Double>, Double> lateRule, AbstractFunction<Double, GradeMnemonic> mnemonicAssignment) {
        this.acceptableRange = acceptableRange;
        this.pointsPossible = pointsPossible;
        this.curve = curve;
        this.lateRule = lateRule;
        this.mnemonicAssignment = mnemonicAssignment;
    }

    public Map<User, Grade> apply(TreeMap<User, Double> defaultGraded) {
        if (getAcceptableRange() != null) {
            defaultGraded.values().forEach(grade -> {
                if (grade < getAcceptableRange()[0] || grade > getAcceptableRange()[1])
                    throw new IllegalArgumentException("grade " + grade + " exceeded specified bounds (" + getAcceptableRange()[0] + ", " + getAcceptableRange()[1] + ").");
            });
        }
        MapListOperation<User> operation = new MapListOperation<>(defaultGraded);
        return null;
    }

    public double[] getAcceptableRange() {
        return acceptableRange;
    }

    public double getPointsPossible() {
        return pointsPossible;
    }

    public NumberListOperation getCurve() {
        return curve;
    }

    public AbstractFunction<List<Double>, Double> getLateRule() {
        return lateRule;
    }

    public AbstractFunction<Double, GradeMnemonic> getMnemonicAssignment() {
        return mnemonicAssignment;
    }
}
