package classes;

import gui.ClassView;
import gui.SidebarHotLink;
import javafx.scene.paint.Color;
import main.Root;
import main.Student;

import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by S507098 on 4/13/2017.
 */

public class ClassPd implements Serializable {

    static final long serialVersionUID = 100L;

    private Course castOf;
    private transient ArrayList<Student> studentList;
    private int periodNo;
    private int capacity;
    private transient Color color;
    private String teacherFirst;
    private String teacherLast;
    private long uniqueId;

    public ClassPd(Course castOf, ArrayList<Student> studentList, int periodNo, int capacity, Color color, String teacherFirst, String teacherLast, long uniqueId) {
        this.castOf = castOf;
        this.studentList = studentList;
        this.periodNo = periodNo;
        this.capacity = capacity;
        this.color = color;
        this.teacherFirst = teacherFirst;
        this.teacherLast = teacherLast;
        this.uniqueId = uniqueId;
        sidebarHotLinks = new ArrayList<>();
        for (int i = 0; i < castOf.getSchedule().getMarkingPeriods(); i++)
            sidebarHotLinks.add(new SidebarHotLink(this, "Grading Period " + (i + 1), null)); //TODO set auto-target
        sidebarHotLinks.add(new SidebarHotLink(this, "Course Information", null));
    }

    private List<SidebarHotLink> sidebarHotLinks;

    public ClassPd() {
        //for debug only
    }

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

    public void fireUpdate(Record.Type type, Date end, String message) {
        //TODO check for existing record chain
        Root.getActiveUser().getUpdates().add(new Record(type, end) {
            {
                ArrayList<RecordEntry> entries = new ArrayList<>();
                entries.add(new RecordEntry(Root.getActiveUser(), message, new Timestamp(System.currentTimeMillis()), this, ClassPd.this));
                setHistory(entries);
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("hello!"); //TODO
            }
        });
    }

    public void startSession() {
        fireUpdate(Record.Type.Session_Start, getTodaysInstance().getEndTime(), null);
    }

    public long getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(long uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public String toString() {
        if (castOf == null)
            return "null " + getPeriodNo();
        return getCastOf().getName() + " - " + getPeriodNo();
    }

    public List<ClassItem> getAssignmentsWithPostsDesc(int i) {
        return new ArrayList<>(); //        TODO
    }

    public List<SidebarHotLink> getActiveSidebarHotLinks() {
        return sidebarHotLinks.stream().filter(SidebarHotLink::isActive).collect(Collectors.toList());
    }


}
