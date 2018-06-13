package classes;

import javafx.scene.paint.Color;
import main.Student;
import main.Teacher;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by S507098 on 4/13/2017.
 */

public class ClassPd implements Serializable {

    static final long serialVersionUID = 100L;

    private Course castOf;
    private ArrayList<Student> studentList;
    private int periodNo;
    private int capacity;
    private transient Color color;
    private String teacherFirst;
    private String teacherLast;



    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public ClassPdInstance getTodaysInstance() {
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

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getTeacherFirst() {
        return teacherFirst;
    }

    public void setTeacherFirst(String teacherFirst) {
        this.teacherFirst = teacherFirst;
    }

    public String getTeacherLast() {
        return teacherLast;
    }

    public void setTeacherLast(String teacherLast) {
        this.teacherLast = teacherLast;
    }
}
