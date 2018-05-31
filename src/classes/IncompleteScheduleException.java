package classes;

public class IncompleteScheduleException extends Exception {
    public IncompleteScheduleException(String s) {
        super(s);
    }
    public IncompleteScheduleException() {
        this("Incomplete Schedule.");
    }
}
