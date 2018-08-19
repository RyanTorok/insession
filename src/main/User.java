package main;

import classes.ClassPd;
import classes.Record;
import classes.setbuilder.Classifiable;
import gui.KeyMap;
import gui.ZipMap;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import module.Module;
import net.Net;
import searchengine.SearchRecord;
import searchengine.WatchRecord;
import terminal.Address;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * Created by 11ryt on 4/21/2017.
 */
public abstract class User implements Classifiable, Serializable {

    static final long serialVersionUID = 42L;

    private static User active = null;

    private String mac;
    private String username;
    private byte[] password;
    private String first;
    private String middle;
    private String last;
    private String email;
    private String schoolCode;
    private Timestamp timestamp;
    private double[] accentColor;
    private ArrayList<Record> updates;
    private Timestamp lastVisit;
    private HashSet<SearchRecord> searchHistory;
    private HashSet<WatchRecord> watchHistory;
    private byte[] passwordSalt;
    private String imageFN;
    private int pictureVisibility;
    private boolean clock24Hour = false;
    private int zipcode;
    private double[] latlon = new double[]{0,0};
    private boolean tempUnits; //false for Metric, true for English
    private HashSet<ClassPd> classesStudent;
    private HashSet<ClassPd> classesTeacher;
    private Timestamp serFileTimestamp = null;
    private String savedFileSeparator = null;
    private transient KeyMap keyMap;
    private double sleepTime;

    public User(String mac, String username, byte[] password, String first, String middle, String last, String email, Timestamp timestamp) {
        this.mac = mac;
        this.username = username;
        this.password = password;
        this.first = first;
        this.middle = middle;
        this.last = last;
        this.email = email;
        this.timestamp = timestamp;
        this.searchHistory = new HashSet<>();
        this.watchHistory = new HashSet<>();
        this.accentColor = new double[]{0, 0, 0};
        this.updates = new ArrayList<>();
        this.classesTeacher = new HashSet<>();
        this.classesStudent = new HashSet<>();
        pictureVisibility = 0;
        sleepTime = 300; //in seconds
    }

    protected User() {

    }

    public static User read(String tokenLabel) {
        File ser = new File(Address.fromRootAddr("usr", tokenLabel + ".ser"));
        if (!ser.exists())
            return null;
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(ser));
            return (User) in.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            return null;
        }
    }

    public static User active() {
        return active;
    }

    public static void setActive(User user) {
        active = user;
    }

    public String getUsername() {
        return username;
    }

    public byte[] getPassword() {
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
        accentColor[0] = c.getRed();
        accentColor[1] = c.getGreen();
        accentColor[2] = c.getBlue();
    }

    public Color getAccentColor() {
        if (accentColor == null) return Color.BLACK;
        return new Color(accentColor[0], accentColor[1], accentColor[2], 1);
    }

    public String getSchoolCode() {
        return schoolCode;
    }

    /**
     * @return the updates
     */
    public ArrayList<Record> getUpdates() {
        if (updates == null) {
            updates = new ArrayList<>();
        }
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
    public void setPassword(byte[] password) {
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
        serFileTimestamp = new Timestamp(System.currentTimeMillis());
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
        searchHistory.add(new SearchRecord(query));
    }

    public void watch(Module m) {
        watchHistory.add(new WatchRecord(m));
    }

    public void rmSearch(String query) {
        searchHistory.removeAll(Collections.singleton(query));
    }

    public void clearSearchHistory() {
        searchHistory.clear();
    }

    public void rmWatch(Module m) {
        watchHistory.removeAll(Collections.singleton(m));
    }

    public void clearWatchHistory() {
        watchHistory.clear();
    }

    public void clearAllHistory() {
        clearSearchHistory();
        clearWatchHistory();
    }

    public static User read() {
        File dir = new File(Address.fromRootAddr("usr"));
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
        File dir = new File(Address.fromRootAddr("usr"));
        File[] sers = dir.listFiles((dir1, name) -> name.endsWith(".ser"));
        ArrayList<User> out = new ArrayList<>();
        if (sers.length == 0) {
            return new ArrayList<>();
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
                    return new ArrayList<>();
                return out;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        }
    }

    public static int getSerCount() {
        File dir = new File(Address.fromRootAddr("usr"));
        File[] sers = dir.listFiles((dir1, name) -> name.endsWith(".ser"));
        return sers.length;
    }

    public Timestamp getLastVisit() {
        return lastVisit;
    }

    public byte[] getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(byte[] passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public Image getAcctImage() {
        try {
            return new Image(getImageFN());
        } catch (Exception e) {
            try {
                return new Image("file:" + Address.fromRootAddr("resources", "default_user.png"));
            } catch (Exception e1) {
                return null;
            }
        }
    }

    public String getImageFN() {
        return imageFN.replaceAll(savedFileSeparator, File.separator);
    }

    public void setImageFN(String imageFN) {
        this.imageFN = imageFN;
        savedFileSeparator = File.separator;
    }

    public boolean isClock24Hour() {
        return clock24Hour;
    }

    public void setClock24Hour(boolean clock24Hour) {
        this.clock24Hour = clock24Hour;
    }

    public double[] getLatlon() {
        return latlon;
    }

    public void setLatlon(double[] latlon) {
        this.latlon = latlon;
    }

    public void setLocation(int zip) throws UnknownZipCodeException {
        this.zipcode = zip;
        ZipMap.LatLon latLon = new ZipMap().get(zip);
        latlon = new double[2];
        try {
            this.latlon[0] = latLon.getLat();
            this.latlon[1] = latLon.getLon();
        } catch (NullPointerException e) {
            throw new UnknownZipCodeException("Zip Code not found: " + zip);
        }
    }

    public boolean usesFahrenheit() {
        return tempUnits;
    }

    public void setTempUnits(boolean tempUnits) {
        this.tempUnits = tempUnits;
    }

    public int getZipcode() {
        return zipcode;
    }

    public HashSet<ClassPd> getClassesTeacher() {
        return classesTeacher;
    }

    public void setClassesTeacher(HashSet<ClassPd> classesTeacher) {
        this.classesTeacher = classesTeacher;
    }

    public HashSet<ClassPd> getClassesStudent() {
        return classesStudent;
    }

    public void setClassesStudent(HashSet<ClassPd> classesStudent) {
        this.classesStudent = classesStudent;
    }

    public void syncExternal(User external) {
        if (external == null)
            return;
        Timestamp me = getSerFileTimestamp(), them = external.getSerFileTimestamp();
        boolean iAmFirst = me.before(them);
        this.username = iAmFirst ? external.getUsername() : username;
        this.password = iAmFirst ? external.getPassword() : password;
        this.passwordSalt = iAmFirst ? external.getPasswordSalt() : passwordSalt;
        this.username = iAmFirst ? external.getUsername() : username;
        this.username = iAmFirst ? external.getUsername() : username;
        this.username = iAmFirst ? external.getUsername() : username;
        this.first = iAmFirst ? external.getFirst() : first;
        this.middle = iAmFirst ? external.getMiddle() : middle;
        this.last = iAmFirst ? external.getLast() : last;
        this.email = iAmFirst ? external.getEmail() : email;
        this.searchHistory.addAll(external.searchHistory);
        this.watchHistory.addAll(external.watchHistory);
        this.accentColor = iAmFirst ? external.accentColor : accentColor;
        this.imageFN = iAmFirst ? external.imageFN : imageFN;
        this.schoolCode = iAmFirst ? external.schoolCode : schoolCode;
        this.zipcode = iAmFirst ? external.zipcode : zipcode;
        this.clock24Hour = iAmFirst ? external.clock24Hour : clock24Hour;
        this.latlon = iAmFirst ? external.getLatlon() : latlon;
        this.tempUnits = iAmFirst ? external.tempUnits : tempUnits;
        this.classesStudent = iAmFirst ? external.getClassesStudent() : getClassesStudent();
        this.classesTeacher = iAmFirst ? external.getClassesTeacher() : getClassesTeacher();
        this.write();
    }

    public Timestamp getSerFileTimestamp() {
        return serFileTimestamp;
    }

    public byte[] getSerFileBytes() {
        File serFile = new File(Address.fromRootAddr("usr", active().getUsername() + ".ser"));
        try {
            FileInputStream input = new FileInputStream(serFile);
            return input.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }

    public void syncSerFileWithServer() {
        syncExternal(Net.syncSerFileDown());
    }

    public void setPictureVisibility(int pictureVisibility) {
        this.pictureVisibility = pictureVisibility;
    }

    public int getPictureVisibility() {
        return pictureVisibility;
    }

    public KeyMap getKeyMap() {
        if (keyMap == null)
            setKeyMap(Root.getPortal().defaultKeyMap());
        return keyMap;
    }

    public void setKeyMap(KeyMap keyMap) {
        this.keyMap = keyMap;
    }

    public double getSleepTime() {
        if (sleepTime == 0.0) {
            setSleepTime(300);
            return 300;
        }
        return sleepTime;
    }

    public void setSleepTime(double sleepTime) {
        this.sleepTime = sleepTime;
    }
}

