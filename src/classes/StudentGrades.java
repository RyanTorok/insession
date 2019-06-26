package classes;

import java.util.TreeMap;

public class StudentGrades {

    private TreeMap<Assignment, Grade> grades;

    public StudentGrades(TreeMap<Assignment, Grade> grades) {
        this.grades = grades;
    }

    public TreeMap<Assignment, Grade> getGrades() {
        return grades;
    }

    public Grade get(Assignment a) {
        return grades.get(a);
    }

    public void put(Assignment a, Grade g) {
        grades.put(a, g);
    }
}
