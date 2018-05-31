package classes;

import main.Student;

import java.util.ArrayList;

/**
 * Created by S507098 on 4/13/2017.
 */

public class ClassPd {


    private Course castOf;
    private ArrayList<Student> studentList;
    private int periodNo;
    private int capacity;

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public classes.ClassPdInstance getTodaysInstance() {
        return null;
    }

    public ArrayList<Student> getStudentList() {
        return studentList;
    }

    public Course getCastOf(){
        return castOf;
    }

    public int getPeriodNo() {
        return periodNo;
    }

    public void setCastOf(Course castOf) {
        this.castOf = castOf;
    }

    public void setStudentList(ArrayList<Student> studentList) {
        this.studentList = studentList;
    }

    public void setPeriodNo(int periodNo) {
        this.periodNo = periodNo;
    }
}
