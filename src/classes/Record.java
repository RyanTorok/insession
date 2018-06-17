package classes;

import gui.Clock;
import main.User;

import java.awt.event.ActionListener;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by 11ryt on 8/19/2017.
 */
public abstract class Record implements ActionListener, Serializable {

    static final Date NEVER = null;

    private static final long serialVersionUID = 101L;
    private ArrayList<RecordEntry> history;
    private Type type;
    private Date autoDestructTime;

    public Record(Type type, Date autoDestructTime) {
        this.history = new ArrayList<>();
        this.type = type;
        this.autoDestructTime = autoDestructTime;
    }

    protected Record() {
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public void createUpdate(String record, User triggeredBy){
        getHistory().add(new RecordEntry(triggeredBy, record, new Timestamp(Clock.currentSafeTime()), this, null));
    }

    public ArrayList<RecordEntry> getHistory() {
        return history;
    }

    public void setHistory(ArrayList<RecordEntry> history) {
        this.history = history;
    }

    public RecordEntry latest() {
        return getHistory().get(getHistory().size() - 1);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Date getAutoDestructTime() {
        return autoDestructTime;
    }

    public void setAutoDestructTime(Date autoDestructTime) {
        this.autoDestructTime = autoDestructTime;
    }

    public enum Type {
        Teacher_Post, Teacher_Repost, Grade, Due_Reminder,  Attendance_Marking, Discussion_Answer, Teacher_Endorsement, New_Assignment_Upload, Office_Request,
        Room_Change, Time_Change, Class_Add, Class_Removal, Class_Change, Automated_Attendance_Request, Secure_Message, Session_Start, Early_Dismissal, Late_Arrival
    }

    public enum Sorting {
        Announcements, Coming_Up, Notifications
    }

    public Sorting getMenuPlacement() {
        switch (type) {
            case Early_Dismissal:
            case Late_Arrival:
            case Due_Reminder:
                return Sorting.Coming_Up;

            case Teacher_Post:
            case Teacher_Repost:
            case Teacher_Endorsement:
                return Sorting.Announcements;

            default: return Sorting.Notifications;
        }
    }

}
