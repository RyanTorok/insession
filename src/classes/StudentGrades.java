package classes;

import java.util.HashMap;

public class StudentGrades {

    private HashMap<Assignment, Grade> grades;

    public HashMap<Assignment, Grade> getGrades() {
        return grades;
    }

    public Grade get(Assignment a) {
        return grades.get(a);
    }

    public void put(Assignment a, Grade g) {
        grades.put(a, g);
    }
}
