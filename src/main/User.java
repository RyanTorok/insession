package main;

import classes.Record;
import javafx.scene.paint.Color;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Created by 11ryt on 4/21/2017.
 */
public abstract class User implements classes.setbuilder.Classifiable{

    private int id;
    private String mac;
    private String username;
    private String password;
    private String first;
    private String middle;
    private String last;
    private String email;
    private String homephone;
    private String cellphone;
    private String schoolCode;
    private String address;
    private Timestamp timestamp;
    private Color accentColor;
    private Date birthday;
    private ArrayList<Record> updates;

    public User(int id, String mac, String username, String password, String first, String middle, String last, String email, String homephone, String cellphone, String address, Timestamp timestamp) {
        this.id = id;
        this.mac = mac;
        this.username = username;
        this.password = password;
        this.first = first;
        this.middle = middle;
        this.last = last;
        this.email = email;
        this.homephone = homephone;
        this.cellphone = cellphone;
        this.address = address;
        this.timestamp = timestamp;
    }

    protected User() {
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFirst() {
        return first;
    }

    public String getMiddle() {
        return middle;
    }

    public String getLast() {
        return last;
    }

    public String getEmail() {
        return email;
    }

    public String getHomePhone() {
        return getHomephone();
    }

    public String getCellphone() {
        return cellphone;
    }

    public String getAddress() {
        return address;
    }


    public Timestamp getTimestamp() {
        return timestamp;
    }

    public abstract String getID();

    public void setAccentColor(Color c) {
        this.accentColor = c;
    }

    public Color getAccentColor() {
        return accentColor;
    }

    public String getSchoolCode() {
        return schoolCode;
    }

    public Date getBirthday() {
        return birthday;
    }

    /**
     * @return the updates
     */
    public ArrayList<Record> getUpdates() {
        return updates;
    }

    /**
     * @param updates the updates to set
     */
    public void setUpdates(ArrayList<Record> updates) {
        this.updates = updates;
    }

    public String getName() {
        return getFirst() + " " + getLast();
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the mac
     */
    public String getMac() {
        return mac;
    }

    /**
     * @param mac the mac to set
     */
    public void setMac(String mac) {
        this.mac = mac;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @param first the first to set
     */
    public void setFirst(String first) {
        this.first = first;
    }

    /**
     * @param middle the middle to set
     */
    public void setMiddle(String middle) {
        this.middle = middle;
    }

    /**
     * @param last the last to set
     */
    public void setLast(String last) {
        this.last = last;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the homephone
     */
    public String getHomephone() {
        return homephone;
    }

    /**
     * @param homephone the homephone to set
     */
    public void setHomephone(String homephone) {
        this.homephone = homephone;
    }

    /**
     * @param cellphone the cellphone to set
     */
    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    /**
     * @param schoolCode the schoolCode to set
     */
    public void setSchoolCode(String schoolCode) {
        this.schoolCode = schoolCode;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @param birthday the birthday to set
     */
    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }
}
