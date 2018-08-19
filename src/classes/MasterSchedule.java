package classes;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;

/**
 * Created by 11ryt on 7/13/2017.
 */
public class MasterSchedule implements Serializable {

    static final long serialVersionUID = 105L;

    private String scheduleCode;
    private ArrayList<MSClass> schedule;
    private int markingPeriods;
    private int currentMarkingPeriod;

    public MasterSchedule(String scheduleRegex) {
        scheduleCode = scheduleRegex;
        schedule = new ArrayList<>();
        //regex format: [classPd(startTime,endTime,nextClassPd)]
        for (int i = 0; i < scheduleRegex.length(); i++) {
            if (scheduleRegex.charAt(i) == '[') {
                int j = i;
                while (scheduleRegex.charAt(j) != ']')
                    j++;
                schedule.add(resolveClassRegex(scheduleRegex.substring(i + 1, j)));
            }
        }
    }

    MasterSchedule() {
        //for debug only
        currentMarkingPeriod = 6;
    }

    private MSClass resolveClassRegex(String regex) {
        Long[] params = new Long[4];
        int paramIndex = 0;
        int lastborderindex = 0;
        for (int i = 0; i < regex.length(); i++) {
            if (regex.charAt(i) == '(' || regex.charAt(i) == ',') {
                params[paramIndex] = Long.parseLong(regex.substring(lastborderindex + 1, i));
                paramIndex++;
                lastborderindex = i;
            }
        }
        return new MSClass(Math.toIntExact(params[0]), params[1], params[2], Math.toIntExact(params[3]));
    }

    public Time getEndTime(int i) {
        return schedule.get(i).getEndTime();
    }

    public String getScheduleCode() {
        return scheduleCode;
    }

    public Integer numPeriods() {
        return schedule.size();
    }

    public int getMarkingPeriods() {
        return markingPeriods;
    }

    public int getCurrentMarkingPeriod() {
        return currentMarkingPeriod;
    }

    public void setMarkingPeriods(int markingPeriods) {
        this.markingPeriods = markingPeriods;
    }
}
