package classes;

import java.sql.Time;

/**
 * Created by 11ryt on 7/13/2017.
 */
public class MSClass {

    private int periodID;
    private Time startTime;
    private Time endTime;
    private int nextPd;


    // the nextPd parameter is positive for normal classes. The nth lunch period is notated as -n, Special Periods are notated in increasing order starting from Integer.Min_Value.
    public MSClass(int periodID, long startTime, long endTime, int nextPd){
        this.periodID = periodID;
        this.startTime = new Time(startTime);
        this.endTime = new Time(endTime);
        this.nextPd = nextPd;
    }

    public long durationMillis(){
        return getEndTime().getTime()- getStartTime().getTime();
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
