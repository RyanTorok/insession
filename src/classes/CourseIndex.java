package classes;

import main.Student;
import main.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

public class CourseIndex {
    //Multi-tiered data structure listing courses indexed by class period, in groups
    HashMap<String, ArrayList<PriorityQueue<ClassPd>>> courses = new HashMap();

    void addClassPd(ClassPd classPd) {
        ArrayList<PriorityQueue<ClassPd>> indexEntry = courses.get(classPd.getCastOf().getClassCode());
        if (indexEntry == null) {
            indexEntry = new ArrayList();
            for (int i = 0; i < School.active.getSchedule().numPeriods(); i++) {
                indexEntry.add(null);
            }
            courses.put(classPd.getCastOf().getClassCode(), indexEntry);
        }

        PriorityQueue<ClassPd> periods = indexEntry.get(classPd.getPeriodNo());
        if (periods == null)
            periods = new PriorityQueue<>((o1, o2) -> { //prioritize emptier classes, used for balancing
                if (o1.getCapacity() - o1.getStudentList().size() > o2.getCapacity() - o2.getStudentList().size())
                    return -1;
                else return 1;
            });
        periods.add(classPd);
    }

    //DO NOT USE THIS TO ADD STUDENTS. IT WILL MESS UP THE PRIORITY STRUCTURE
    ClassPd peekEmptiestClass(Course course, Integer period) {
        ClassPd candidate = courses.get(course.getClassCode()).get(period).peek();
        if (candidate.getCapacity() <= candidate.getStudentList().size())
            return null;
        return candidate;
    }

    void addToEmptiest(User s, Course course, Integer period) {
        PriorityQueue<ClassPd> q = courses.get(course.getClassCode()).get(period);
        ClassPd empty = q.poll();
        empty.getStudentList().add(s);
        q.add(empty);
    }

    void remove(User swapGuy, Course course, Integer period) {
        for (ClassPd cp : courses.get(course.getClassCode()).get(period)
             ) {
            cp.getStudentList().remove(swapGuy);
        }
    }
}
