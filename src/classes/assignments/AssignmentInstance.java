package classes.assignments;

import main.User;

public class AssignmentInstance {
    private Assignment assignment;
    private User student;


    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }
}
