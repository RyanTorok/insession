package classes;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * Created by S507098 on 4/19/2017.
 */
public abstract class Assignment extends Record{

    private Course belongsToCourse;
    private ArrayList<Attempt> attempts;
    private String name;

    public Assignment(String name){
        attempts = new ArrayList<>();
        this.name = name;
        actionPerformed(null);
    }

    public void newAttempt(Curve c){
        attempts.add(new Attempt(c));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e == null){

        }
    }

    public String getName() {
        return name;
    }

    public Course getBelongsToCourse() {
        return belongsToCourse;
    }

    public void setBelongsToCourse(Course belongsToCourse) {
        this.belongsToCourse = belongsToCourse;
    }
}
