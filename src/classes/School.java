package classes;

/**
 * Created by 11ryt on 7/19/2017.
 */
public class School {

    public static School active() {
        return active;
    }

    public MasterSchedule getSchedule() {
        return schedule;
    }

    static School active;
    private String name;
    private MasterSchedule schedule;

    public School(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
