package classes;

import java.util.HashMap;
import main.Student;

/**
 * Created by S507098 on 4/13/2017.
 */
public class Attempt {
    private Curve curve;
    private HashMap<Student, Double> grades;

    public Attempt(Curve c) {
        grades = new HashMap();
        curve = c;
    }

    public void gradeStudent(Student s, Double grade) {
        grades.put(s, grade);
    }

}
