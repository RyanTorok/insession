package classes.setbuilder;

import exceptions.ExpressionSyntaxException;
import main.Student;
import main.User;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by 11ryt on 6/9/2017.
 */
public class Set extends HashSet implements Classifiable {
    private String name;
    private Condition condition;
    private ArrayList<Classifiable> users;

    public Set(String name, Condition cond){
        this(name, new ArrayList(), cond);
    }

    private Set(String name, ArrayList<Classifiable> elements, Condition condition){
        this.name = name;
        users = elements;
        this.condition = condition;
        ArrayList<Classifiable> matches = condition.getAllMatches();
        for (Classifiable cl : matches) {
            this.add(cl);
        }
    }

    public Set() {
        name = "untitled set";
        condition = null;
        users = new ArrayList<>();
    }

    public boolean existsIn(User e) throws ExpressionSyntaxException {
        return condition.evaluate(e);
    }

}
