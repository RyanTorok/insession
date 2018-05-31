package exceptions;

/**
 * Created by 11ryt on 6/21/2017.
 */
public class DatabaseException extends Throwable {

    public DatabaseException(String s) {
        super(s);
    }
    public DatabaseException(){
        super();
    }
}
