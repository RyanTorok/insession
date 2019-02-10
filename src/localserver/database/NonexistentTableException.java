package localserver.database;

public class NonexistentTableException extends Exception {

    public NonexistentTableException() {}

    public NonexistentTableException(String message) {
        super(message);
    }
}
