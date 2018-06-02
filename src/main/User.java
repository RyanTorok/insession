package main;

import classes.Record;
import javafx.scene.paint.Color;
import terminal.Address;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by 11ryt on 4/21/2017.
 */
public abstract class User implements classes.setbuilder.Classifiable, Serializable {

    static final long serialVersionUID = 42L;
    private String mac;
    private String username;
    private String password;
    private String first;
    private String middle;
    private String last;
    private String email;
    private String schoolCode;
    private Timestamp timestamp;
    private double[] accentColor;
    private ArrayList<Record> updates;
    private Timestamp lastVisit;
    private ArrayList<String> searchHistory;
    private ArrayList<module.Module> watchHistory;
    private String passwordSalt;

    public User(String mac, String username, String password, String first, String middle, String last, String email, Timestamp timestamp) {
        this.mac = mac;
        this.username = username;
        this.password = password;
        this.first = first;
        this.middle = middle;
        this.last = last;
        this.email = email;
        this.timestamp = timestamp;
        this.searchHistory = new ArrayList<>();
        this.watchHistory = new ArrayList<>();
    }

    protected User() {

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

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public abstract String getID();

    public void setAccentColor(Color c) {
        accentColor = new double[]{c.getRed(), c.getGreen(), c.getBlue()};
    }

    public Color getAccentColor() {
        return new Color(accentColor[0], accentColor[1], accentColor[2], 1);
    }

    public String getSchoolCode() {
        return schoolCode;
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
     * @param schoolCode the schoolCode to set
     */
    public void setSchoolCode(String schoolCode) {
        this.schoolCode = schoolCode;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void write() {
        lastVisit = new Timestamp(System.currentTimeMillis());
        try {
            File dest = new File("usr" + File.separator + this.getUsername() + ".ser");
            if (!dest.exists()) {
                boolean b = dest.createNewFile();
                if (!b) {
                    throw new IOException("an error occurred creating user file for '" + getUsername() + "'.");
                }
            }
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(dest));
            out.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void search(String query) {
        searchHistory.add(query);
    }

    public void watch(module.Module m) {
        watchHistory.add(m);
    }

    public void rmSearch(String query) {
        searchHistory.removeAll(Collections.singleton(query));
    }

    public void rmSearch(int index) {
        rmSearch(searchHistory.get(index));
    }

    public void clearSearchHistory() {
        searchHistory.clear();
    }

    public void rmWatch(module.Module m) {
        watchHistory.removeAll(Collections.singleton(m));
    }

    public void rmWatch(int index) {
        rmWatch(watchHistory.get(index));
    }

    public void clearWatchHistory() {
        watchHistory.clear();
    }

    public void clearAllHistory() {
        clearSearchHistory();
        clearWatchHistory();
    }

    public static User read() {
        File dir = new File(Address.root_addr.getPath() + File.separator + "usr");
        File[] sers = dir.listFiles((dir1, name) -> name.endsWith(".ser"));
        if (sers.length == 0) {
            return null;
        } else {
            String defaultFN = new DefaultUser().read();
            User[] users = new User[sers.length];
            try {
                for (int i = defaultFN == null ? 0 : -1 ; i < sers.length; i++) {
                    //read default user first
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream((i == -1) ? new File(defaultFN) : sers[i]));
                    Object readin = in.readObject();
                    try {
                        users[i] = (User) readin;
                    } catch (ClassCastException e) {
                        users[i] = null;
                    }
                    if (users[i] != null) {
                        return users[i];
                    }
                }
                return null;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static ArrayList<User> readAll() {
        File dir = new File(Address.root_addr.getPath() + File.separator + "usr");
        File[] sers = dir.listFiles((dir1, name) -> name.endsWith(".ser"));
        ArrayList<User> out = new ArrayList<>();
        if (sers.length == 0) {
            return null;
        } else {
            String defaultFN = new DefaultUser().read();
            User[] users = new User[sers.length];
            try {
                for (int i = defaultFN == null ? 0 : -1 ; i < sers.length; i++) {
                    //read default user first
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream((i == -1) ? new File(defaultFN) : sers[i]));
                    Object readin = in.readObject();
                    try {
                        users[i] = (User) readin;
                    } catch (ClassCastException e) {
                        users[i] = null;
                    }
                    if (users[i] != null) {
                        out.add(users[i]);
                    }
                }
                if (out.size() == 0)
                    return null;
                return out;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static int getSerCount() {
        File dir = new File(Address.root_addr.getPath() + File.separator + "usr");
        File[] sers = dir.listFiles((dir1, name) -> name.endsWith(".ser"));
        return sers.length;
    }

    public Timestamp getLastVisit() {
        return lastVisit;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }
}

