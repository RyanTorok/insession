package classes;

public class  StudentSchedule {
    Course[] courseOrder;

    public StudentSchedule() {
        this.courseOrder = new Course[School.active.getSchedule().numPeriods()];
    }

    void setCourse(int period, Course c) {
        courseOrder[period] = c;
    }

    void verify() throws IncompleteScheduleException {
        for (int x = 0;  x < courseOrder.length; x++) {
            if (courseOrder[x] == null)
                throw new IncompleteScheduleException("Incomplete Schedule. Missing Class for Period " + x);
        }
    }
}