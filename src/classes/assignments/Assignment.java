package classes.assignments;

import classes.ClassItem;
import classes.ClassPd;
import classes.Grade;
import main.User;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Assignment extends ClassItem {
    private String name;
    private ClassPd forClass;
    GradeSchema gradingRules;

    private void gradeAll(Grade... grades) {
        gradingRules.apply(gradeAllDefault(Arrays.stream(grades).map(Grade::getValue).collect(Collectors.toList())));

    }

    private TreeMap<User, Double> gradeAllDefault(List<Double> grades) {
        TreeMap<User, Double> result = new TreeMap<>();
        int i = 0;
        final int size = grades.size();
        for (User u : forClass.getStudentList()) {
            result.put(u, grades.get(i));
            i++;
            if (i >= size)
                break;
        }
        return result;
    }
}
