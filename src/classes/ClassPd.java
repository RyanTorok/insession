package classes;

import localserver.IDAllocator;
import gui.SidebarHotLink;
import javafx.scene.paint.Color;
import main.*;
import net.PostEngine;
import org.json.JSONObject;
import searchengine.Identifier;
import searchengine.Indexable;
import searchengine.RankedString;

import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by S507098 on 4/13/2017.
 */

public class ClassPd extends MSClass implements Serializable, Indexable {

    static final long serialVersionUID = 100L;

    private Course castOf;
    private transient ArrayList<User> studentList;
    private int periodNo;
    private int capacity;
    private transient Color color;
    private String teacherFirst;
    private String teacherLast;
    private Gradebook gradebook;
    private PostEngine postEngine;
    private Teacher teacher;
    private Identifier identifier;

    public ClassPd(Course castOf, ArrayList<User> studentList, int periodNo, int capacity, Color color, Teacher teacher, UUID uniqueId) {
        this.castOf = castOf;
        this.studentList = studentList;
        this.periodNo = periodNo;
        this.capacity = capacity;
        this.color = color;
        this.teacherFirst = teacherFirst;
        this.teacherLast = teacherLast;
        initSidebarHotLinks();
        gradebook = new Gradebook();
        postEngine = new PostEngine(this);
        this.teacher = teacher;
        identifier = new Identifier(toString(), Identifier.Type.Class, uniqueId);
    }

    public static ClassPd fromId(UUID classId) {
        return null;
    }

    private void initSidebarHotLinks() {
        sidebarHotLinks = new ArrayList<>();
        for (int i = 0; i < castOf.getSchedule().getMarkingPeriods(); i++)
            sidebarHotLinks.add(new SidebarHotLink(this, "Grading Period " + (i + 1), null)); //TODO set auto-target
        sidebarHotLinks.add(new SidebarHotLink(this, "Course Information", null));
    }

    private List<SidebarHotLink> sidebarHotLinks;

    public ClassPd() {
        //for debug only
        castOf = new Course() {{setName("Test Class");}};
        castOf.setSchedule(new CourseSchedule() {{setMarkingPeriods(6);}});
        setColor(Color.FORESTGREEN);
        setPeriodNo(4);
        initSidebarHotLinks();
        gradebook = new Gradebook();
        postEngine = new PostEngine(this);
        identifier = new Identifier(toString(), Identifier.Type.Class, 0);
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

    public ArrayList<User> getStudentList() {
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

    public void setStudentList(ArrayList<User> studentList) {
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
        User.active().getUpdates().add(new Record(type, end) {
            {
                ArrayList<RecordEntry> entries = new ArrayList<>();
                entries.add(new RecordEntry(User.active(), message, new Timestamp(System.currentTimeMillis()), this, ClassPd.this));
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

    public UUID getUniqueId() {
        return identifier.getId();
    }

    public void setUniqueId(UUID uniqueId) {
        identifier.setId(uniqueId);
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


    public Gradebook getGradebook() {
        return gradebook;
    }

    public void setGradebook(Gradebook gradebook) {
        this.gradebook = gradebook;
    }

    public PostEngine getPostEngine() {
        return postEngine;
    }

    public Color textFill() {
        return Colors.textFill(getColor());
    }

    public Teacher getTeacher() {
        return teacher;
    }

    @Override
    public Timestamp lastIndexed() {
        return null;
    }

    @Override
    public List<RankedString> getIndexTextSets() {
        ArrayList<RankedString> toIndex = new ArrayList<>();
        toIndex.add(new RankedString(castOf.getName(), TITLE_RELEVANCE));
        toIndex.add(new RankedString(identifier.getId().toString(), HEADER_RELEVANCE));
        toIndex.add(new RankedString(teacherFirst, HEADER_RELEVANCE));
        toIndex.add(new RankedString(teacherLast, HEADER_RELEVANCE));
        return toIndex;
    }

    @Override
    public Identifier getUniqueIdentifier() {
        return identifier;
    }

    @Override
    public void launch() {
        Root.getPortal().launchClass(this);
    }

    @Override
    public JSONObject toJSONObject() {
        return null;
    }
}
