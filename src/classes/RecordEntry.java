package classes;

import main.User;

import java.sql.Timestamp;

/**
 * Created by 11ryt on 8/22/2017.
 */
public class RecordEntry {
    private User triggeredBy;
    private Object entry;
    private Timestamp timestamp;

    public RecordEntry(User triggeredBy, Object entry, Timestamp timestamp){
        this.triggeredBy = triggeredBy;
        this.entry = entry;
        this.timestamp = timestamp;
    }
    
    public String toString() {
        return triggeredBy.getName() + entry + "at " + timestamp.toString();
    }
}
