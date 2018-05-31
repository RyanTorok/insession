package classes;

import main.Student;

public class SSPBuilder {
    StudentSchedule schedule = new StudentSchedule();

    Student student;

    public SSPBuilder(Student s) {
        student = s;
    }

    UtilScheduler.StudentSchedulePackage build() throws IncompleteScheduleException {
        schedule.verify();
        UtilScheduler.StudentSchedulePackage ssp = new UtilScheduler.StudentSchedulePackage(student, schedule);
        student.setSavedSSP(ssp);
        return ssp;
    }

    void setPeriod(int period, Course c) {
        schedule.setCourse(period, c);
    }
}
