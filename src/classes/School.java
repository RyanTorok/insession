package classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by 11ryt on 7/19/2017.
 */
public class School implements Serializable {

    static final long serialVersionUID = 104L;

    static School active;

    private String name;
    private MasterSchedule schedule;

    public static School active() {
        return active;
    }

    public MasterSchedule getSchedule() {
        return schedule;
    }


    public School(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    //for debug only
    public static void initActiveDebug() {
        active = new School("Test School") {{

        }};
        active.schedule = new MasterSchedule();
        active.schedule.setMarkingPeriods(6);
    }
}
