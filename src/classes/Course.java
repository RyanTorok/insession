package classes;

import java.util.ArrayList;

/**
 * Created by S507098 on 4/13/2017.
 */

public class Course {
    private String name;
    private String alias;
    private String classCode;
    private boolean[] offeredPds;
    private boolean totalStudents;
    private String[] roomNo;
    private int[] classroomSize;
    private Course combinedWith;
    private ClassPd[] classes;

    private MarkingPeriod[] markingPeriods;

    public void newGrade(String name, ArrayList<Attempt> attempts){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public boolean[] getOfferedPds() {
        return offeredPds;
    }

    public void setOfferedPds(boolean[] offeredPds) {
        this.offeredPds = offeredPds;
    }

    public boolean isTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(boolean totalStudents) {
        this.totalStudents = totalStudents;
    }

    public String[] getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String[] roomNo) {
        this.roomNo = roomNo;
    }

    public int[] getClassroomSize() {
        return classroomSize;
    }

    public void setClassroomSize(int[] classroomSize) {
        this.classroomSize = classroomSize;
    }

    public Course getCombinedWith() {
        return combinedWith;
    }

    public void setCombinedWith(Course combinedWith) {
        this.combinedWith = combinedWith;
    }

    public ClassPd[] getClasses() {
        return classes;
    }

    public void setClasses(ClassPd[] classes) {
        this.classes = classes;
    }
}
