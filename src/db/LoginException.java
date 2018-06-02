package db;

public class LoginException extends Exception {
    public LoginException() {
        this(false);
    }
    public LoginException(boolean connectionError) {
        super(Boolean.toString(connectionError));
    }
}
