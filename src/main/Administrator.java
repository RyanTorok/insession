package main;

import java.sql.Timestamp;

/**
 * Created by 11ryt on 4/21/2017.
 */
public class Administrator extends User {
    static long serialVersionUID = User.serialVersionUID;
    private String eid;

    public Administrator(String mac, String username, byte[] password, String first, String middle, String last, String email, String eid, Timestamp timestamp) {
        super(mac, username, password, first, middle, last, email, timestamp);
        this.eid = eid;
    }

    @Override
    public String getID() {
        return eid;
    }
}
