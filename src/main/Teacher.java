package main;

import java.sql.Timestamp;

/**
 * Created by 11ryt on 4/21/2017.
 */
public class Teacher extends User {

    private String eid;
    public Teacher(int id, String mac, String username, String password, String first, String middle, String last, String email, String homephone, String cellphone, String address, String eid, Timestamp timestamp) {
        super(id, mac, username, password, first, middle, last, email, homephone, cellphone, address, timestamp);
        this.eid = eid;
    }

    public Teacher(int id, String mac, String username, String password, String first, String middle, String last, String email, String homePhone, String cellPhone, String address, String schoolCode, String studentID, Timestamp timestamp) {
    }

    @Override
    public String getID() {
        return eid;
    }
}
