package classes;

import main.Root;
import main.Student;

import java.sql.Time;
import java.util.ArrayList;

/**
 * Created by 11ryt on 7/13/2017.
 */
public class ClassPdInstance {
    private ClassPd castOf;
    private ArrayList<AttendanceCode> attendance;
    private Time startTime;
    private Time endTime;

    public boolean isPresent(Student e){
        return getAttendance().get(getCastOf().getStudentList().indexOf(e)).equals(Root.getUtilAndConstants().getAttendanceCodes(0));
    }

    public ClassPd getCastOf() {
        return castOf;
    }

    public void setCastOf(ClassPd castOf) {
        this.castOf = castOf;
    }

    public ArrayList<AttendanceCode> getAttendance() {
        return attendance;
    }

    public void setAttendance(ArrayList<AttendanceCode> attendance) {
        this.attendance = attendance;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }
}
