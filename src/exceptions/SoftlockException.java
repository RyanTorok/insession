package exceptions;

/**
 * Created by 11ryt on 7/21/2017.
 */

//thrown when the program is stuck because the user caused a thread carrying requested information to terminate prematurely.

public class SoftlockException extends Exception {
    public SoftlockException(String s) {
        super(s);
        System.exit(-1);
    }
}
