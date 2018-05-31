package classes.setbuilder;

import classes.SQL;
import exceptions.ExpressionSyntaxException;
import main.User;

import java.util.ArrayList;

/**
 * Created by 11ryt on 6/9/2017.
 */
public class Condition {
    private String root;
    private Condition[] split;
    private boolean andor; //true for and, false for or, automatically false if in base case.

    public Condition(String s) throws ExpressionSyntaxException {
        split = new Condition[2];
        root = s;
        if (root.length() == 0 || root.equals("()")) {
            throw new ExpressionSyntaxException("Invalid Conditional Expression: Empty expression or empty parentheses");
        }
        if (root.charAt(0) == '(' && root.charAt(root.length() - 1) == ')') {
            root = root.substring(1, root.length() - 1);
        }
        boolean splitup = false;
        for (int x = 0; x < root.length(); x++) {

            switch (root.charAt(x)) {
                case 'o': {
                    splitup = true;
                    split[0] = new Condition(root.substring(0, x));
                    split[1] = new Condition(root.substring(x + 1));
                    andor = false;
                }
                break;
                case 'a': {
                    splitup = true;
                    split[0] = new Condition(root.substring(0, x));
                    split[1] = new Condition(root.substring(x + 1));
                    andor = true;
                }
                break;
                case '(': {
                    int parenOffset = 1;
                    int i = x + 1;
                    try {
                        for (; parenOffset > 0; i++) {
                            parenOffset += (root.charAt(i) == '(') ? 1 : 0;
                            parenOffset -= (root.charAt(i) == ')') ? 1 : 0;
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new ExpressionSyntaxException("Invalid Conditional Expression: Unclosed Parentheses");
                    }
                    x = i;
                }
                break;
            }
        }
        if (!splitup) {
            split = null;
        }
        andor = false;
    }

    private Condition(String sub, boolean andor) throws ExpressionSyntaxException {
        this(sub);
        this.andor = andor;
    }

    public boolean evaluate(User u) throws ExpressionSyntaxException {
        if (this.split[1] != null) {
            return (andor) ? split[0].evaluate(u) && split[1].evaluate(u) : split[0].evaluate(u) || split[1].evaluate(u);
        } else {
            try {
                Integer functionKey = Integer.parseInt(root.substring(0, root.indexOf("[")));
                return multiInputBoolean(functionKey, (root.substring(root.indexOf("[")+1, root.lastIndexOf("]"))).split(","), u);
            } catch (NumberFormatException ex) {
                throw new ExpressionSyntaxException("Invalid Conditional Expression: Unrecognized function key \"" + root.substring(0, root.indexOf("[")) + "\"");
            }
        }
    }

    private boolean multiInputBoolean(Integer functionKey, String[] split, User u) throws ExpressionSyntaxException {
        /*
        Function Keys:
        0: Exists in pre-existing set (setName)
        1: Student has class (classID), (classID, Period);
        2: Student assignment grade (comparisonRegex, AssignmentID)
        3: Student current marking period average (comparisonRegex, markingPdNo, ClassID)
        4: Student progress report average (comparisonRegex, markingPdNo, ClassID)
        5: Student report card average (comparisonRegex, markingPdNo, ClassID)
        6: Student semester/trimester average (comparisonRegex, semesterNo, ClassID)
        7: Student final average (comparisonRegex, ClassID)
        8: Student marking period absences (comparisonRegex, markingPdNo, ClassID, attendanceCodes)
        9: Student semester/trimester absences (comparisonRegex, semesterNo, ClassID, attendanceCodes)
       10: Student year absences (comparisonRegex, ClassID, attendanceCodes)
       - a * entered for the class id in functions 3-10 signify overall average/total of all classes for their respective time frames.
       11: Student Test Question Response (comparisonRegex, assignmentID, numberOnKey)
       12:
         */
        switch(functionKey){
            case 0: return SQL.getSet(split[0]).existsIn(u);
            default: return false;
        }
    }

    public ArrayList<Classifiable> getAllMatches(){
        ArrayList<Classifiable> matches = new ArrayList<>();
        for(User u : SQL.getMasterUserList()){
            try {
                if(evaluate(u)){
                    matches.add(u);
                }
            } catch (ExpressionSyntaxException e) {
                e.printStackTrace();
            }
        }
        return matches;
    }
}
