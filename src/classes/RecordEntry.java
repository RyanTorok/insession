package classes;

import main.User;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by 11ryt on 8/22/2017.
 */
public class RecordEntry implements Comparable<RecordEntry>, Serializable {

    static final long serialVersionUID = 102L;
    private User triggeredBy;
    private ClassPd belongsTo;
    private Object entry;
    private Timestamp timestamp;
    private Record chain;

    public RecordEntry(User triggeredBy, Object entry, Timestamp timestamp, Record chain, ClassPd belongsTo){
        this.setTriggeredBy(triggeredBy);
        this.setEntry(entry);
        this.setTimestamp(timestamp);
        this.chain = chain;
        this.belongsTo = belongsTo;
    }
    
    public String toString() {
        return getTriggeredBy().getName() + getEntry() + "at " + getTimestamp().toString();
    }

    public User getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(User triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public Object getEntry() {
        return entry;
    }

    public void setEntry(Object entry) {
        this.entry = entry;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(RecordEntry o) {
        return -1 * getTimestamp().compareTo(o.getTimestamp());
    }

    public String toMessage() {
        return "Some update happened";
    }

    public ClassPd getBelongsTo() {
        return belongsTo;
    }

    public void setBelongsTo(ClassPd belongsTo) {
        this.belongsTo = belongsTo;
    }

    public Record getChain() {
        return chain;
    }

    public void fireEvent() {
        System.out.println("fire event for record from " + belongsTo.getCastOf().getName());
        //TODO implement this
    }
}
