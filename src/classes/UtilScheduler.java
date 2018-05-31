package classes;

import gui.Clock;
import main.Student;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class UtilScheduler {

    List<StudentSchedulePackage> students;
    static final Comparator<StudentSchedulePackage> randomSSPC = (s1, s2) -> Math.random() < .5 ? 1 : -1;
    static final Comparator<StudentSchedulePackage> byYearDescSSPC = (o1, o2) -> o1.student.getGrade() > o2.student.getGrade() ? -1 : 1;
    static final Comparator<StudentSchedulePackage> byAlpha = Comparator.comparing(o -> o.student.getLast());

    CourseIndex index;

    public void run() {
        for (int i = 0; i < School.active.getSchedule().numPeriods(); i++) {
            for (int j = 0; j < students.size(); j++) {
                index.addToEmptiest(students.get(j).student, students.get(j).proposedSchedule.courseOrder[i], i);
            }
        }
        ArrayList<Course> overfilledCourses = new ArrayList<>();
        for (ArrayList<PriorityQueue<ClassPd>> allPds : index.courses.values()) {
            for (PriorityQueue<ClassPd> pq : allPds) {
                for (ClassPd period : pq) {
                    while (period.getCapacity() < period.getStudentList().size()) {
                        Integer nearestHole = -1, checkOffset = 1;
                        while (nearestHole == -1 && checkOffset < School.active.getSchedule().numPeriods()) {
                            ClassPd checking = (index.peekEmptiestClass(period.getCastOf(), period.getPeriodNo() + checkOffset));
                            if (checking != null)
                                nearestHole = (checking.getStudentList().size() < checking.getCapacity()) ? checking.getPeriodNo() : nearestHole;

                            checking = (index.peekEmptiestClass(period.getCastOf(), period.getPeriodNo() - checkOffset));
                            if (checking != null)
                                nearestHole = (checking.getStudentList().size() < checking.getCapacity()) ? checking.getPeriodNo() : nearestHole;
                        }
                        if (nearestHole == -1) { //no hole exists, class is overfilled by the pigeonhole principle
                            overfilledCourses.add(period.getCastOf());
                            continue;
                        }
                        ArrayList<Integer> studentScores = new ArrayList<>();
                        for (Student s : period.getStudentList()) {
                            StudentSchedulePackage ssp = s.getSavedSSP();
                            ClassPd swapClass = index.courses.get(ssp.proposedSchedule.courseOrder[nearestHole].getClassCode()).get(period.getPeriodNo()).peek();
                            studentScores.add(swapClass.getCapacity() - swapClass.getStudentList().size());
                        }
                        Integer min = Integer.MAX_VALUE, index0 = 0, minIndex = -1;
                        for (Integer i : studentScores) {
                            if (i < min) {
                                min = i;
                                minIndex = index0;
                            }
                            index0++;
                        }
                        //the student whose proposed class at the location of the hole is the emptiest in the period containing the overfull class
                        Student swapGuy = period.getStudentList().get(minIndex);

                        //swap student class times
                        index.remove(swapGuy, swapGuy.getSavedSSP().proposedSchedule.courseOrder[period.getPeriodNo()], period.getPeriodNo());
                        index.remove(swapGuy, swapGuy.getSavedSSP().proposedSchedule.courseOrder[nearestHole], nearestHole);
                        index.addToEmptiest(swapGuy, swapGuy.getSavedSSP().proposedSchedule.courseOrder[period.getPeriodNo()], nearestHole);
                        index.addToEmptiest(swapGuy, swapGuy.getSavedSSP().proposedSchedule.courseOrder[nearestHole], period.getPeriodNo());
                    }
                }
            }
        }
    }

    public static class StudentSchedulePackage {
        Timestamp timeSubmitted;
        Student student;
        StudentSchedule proposedSchedule;

        public StudentSchedulePackage(Student student, StudentSchedule proposedSchedule) {
            this.student = student;
            this.timeSubmitted = new Timestamp(Clock.currentSafeTime());
        }

        Student getStudent() {
            return student;
        }

        Timestamp getTimeSubmitted() {
            return timeSubmitted;
        }
    }

    void sortRandom() {
        students.sort(randomSSPC);
    }

    void sortDescYearThenRandom() {
        students.sort(byYearDescSSPC.thenComparing(randomSSPC));
    }

    void sortAlpha() {
        students.sort(byAlpha);
    }

    void sortDescYearThenAlpha() {
        students.sort(byYearDescSSPC.thenComparing(byAlpha));
    }

    void sortAscYearThenRandom() {
        students.sort(byYearDescSSPC.reversed().thenComparing(randomSSPC));
    }

    void sortAscYearThenAlpha() {
        students.sort(byYearDescSSPC.reversed().thenComparing(byAlpha));
    }

    void sortTimeSubmitted() { //oldest first
        students.sort(Comparator.comparing(StudentSchedulePackage::getTimeSubmitted));
    }

    void sortDescYearThenTime() {
        students.sort(byYearDescSSPC.thenComparing(StudentSchedulePackage::getTimeSubmitted));
    }

    void sortAscYearThenTime() {
        students.sort(byYearDescSSPC.reversed().thenComparing(StudentSchedulePackage::getTimeSubmitted));
    }
}