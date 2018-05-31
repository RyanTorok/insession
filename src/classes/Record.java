package classes;

import gui.Clock;
import main.User;

import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Created by 11ryt on 8/19/2017.
 */
public abstract class Record implements ActionListener{
    private ArrayList<RecordEntry> history;

    protected Record() {
        
    }

    public void createUpdate(String record, User triggeredBy){
        history.add(new RecordEntry(triggeredBy, record, new Timestamp(Clock.currentSafeTime())));
    }
}
