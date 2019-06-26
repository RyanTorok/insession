package classes;

import exceptions.Warning;
import main.Student;
import main.User;

import java.awt.event.ActionEvent;
import java.text.DecimalFormat;

/**
 * Created by 11ryt on 5/23/2017.
 */
public class Grade extends Record {

    private double value = Double.MIN_VALUE;
    private double pointsPossible = 100;
    private GradeCategory category;
    private double acceptableRange[];
    private Assignment belongsTo;
    private int roundBehavior; // 0: nothing, 1: always round normally, 2: always floor, 3: always ceil

    public Grade(double lb, double ub,  double pointsPossible, Assignment belongsTo, boolean alwaysRound){
        acceptableRange = new double[2];
        acceptableRange[0] = Math.min(lb, ub);
        acceptableRange[1] = Math.max(lb, ub);
    }

    public Grade(Double value) {
        this.value = value;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //notify all students in course of assignment change
        ClassPd[] cps = belongsTo.getBelongsToCourse().getClasses();
        for (ClassPd cp: cps) {
            for (User s : cp.getStudentList()) {
                this.createUpdate(((e == null) ? "recorded" : "updated") + "grade for " + belongsTo.getName() + ": " + castInt(value) + "out of " + castInt(pointsPossible) + ".", s);
            }
        }
    }

    private String castInt(double value) {
        if(roundBehavior == 2){
            value = Math.floor(value);
        }
        if(roundBehavior == 3){
            value = Math.ceil(value);
        }
        return (Math.floor(value) == value || roundBehavior != 0) ? String.valueOf((int) Math.round(value)) : String.valueOf(value);
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) throws IllegalArgumentException, Warning {
        if (value == Double.MIN_VALUE) {
            throw new IllegalArgumentException("Illegal Grade Value.");
        } else {
            if (value < acceptableRange[0] || value > acceptableRange[1]) {
                throw new Warning("Grade outside specified range.");
            } else {
                if (this.value == Double.MIN_VALUE)
                    actionPerformed(null);
                else
                    actionPerformed(new ActionEvent(this, 0, ""));
                this.value = value;
            }
        }
    }

    public GradeCategory getCategory() {
        return category;
    }

    public void setCategory(GradeCategory category) {
        this.category = category;
    }

    private static final DecimalFormat format = new DecimalFormat("####0.##");

    public String getDisplayText(User user) {
        return format.format(value); //TODO add support for user-level and class-level mnemonics
    }

    public String getDisplayTextOutOf(User user, boolean longForm) {
        return getDisplayText(user) + " out of " + format.format(pointsPossible);
    }
}
