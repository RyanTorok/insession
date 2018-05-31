package main;

/**
 * Created by 11ryt on 4/29/2017.
 */

public class Bluetooth {
    private boolean markPresent(classes.ClassPd activeClass, Student s){
       return s.markPresent(activeClass, new java.sql.Time(System.currentTimeMillis()));
    }
}
