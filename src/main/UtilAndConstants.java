package main;

import classes.MasterSchedule;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.ArrayList;

/**
 * Created by 11ryt on 7/3/2017.
 */
public class UtilAndConstants {

    /**
     * @return the operatingSystem
     */
    public static String getOperatingSystem() {
        if (operatingSystem == null)
            initOS();
        return operatingSystem;
    }

    /**
     * @param aOperatingSystem the operatingSystem to set
     */
    public static void setOperatingSystem(String aOperatingSystem) {
        operatingSystem = aOperatingSystem;
    }

    /**
     * @param aStartScreenTimeFormat the startScreenTimeFormat to set
     */
    public static void setStartScreenTimeFormat(String aStartScreenTimeFormat) {
        startScreenTimeFormat = aStartScreenTimeFormat;
    }

    private int MAX_ATTENDANCE_EXTRA_TIME;
    private ArrayList<String> attendanceCodes;
    private Time attendanceStartTime;
    private static String operatingSystem;
    
    private static String startScreenTimeFormat;

    public UtilAndConstants(int mAET, ArrayList<String> attendanceCodes, Time attendanceStartTime) {
        MAX_ATTENDANCE_EXTRA_TIME = mAET;
        this.attendanceCodes = attendanceCodes;
        this.attendanceStartTime = attendanceStartTime;
    }

    private static void initOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if(os.indexOf("win") >= 0)
            setOperatingSystem("win");
        else if(os.indexOf("linux") >= 0)
            setOperatingSystem("linux");
        else if (os.indexOf("mac") >= 0)
            setOperatingSystem("mac");
    }

    public int getMAX_ATTENDANCE_EXTRA_TIME() {
        return MAX_ATTENDANCE_EXTRA_TIME;
    }

    public long elapsedTimeMillis(Time earlierTime, Time laterTime) {
        return laterTime.getTime() - earlierTime.getTime();
    }

    public BigDecimal elapsedTimeUnits(Time earlierTime, Time laterTime, byte convertTo) {
        //convertTo Key:
        //0: millisecond (1/1000 second) - returns original value
        //1: centisecond (1/100 second)
        //2: decisecond  (1/10 second)
        //3: second
        //4: minute
        //5: hour
        //6: day
        //7: year
        //8: decade
        //9: century
        //10: millennium
        return convertTimeUnits(elapsedTimeMillis(earlierTime, laterTime), convertTo);
    }

    private BigDecimal convertTimeUnits(long millis, byte convertTo) {
        BigDecimal bd = new BigDecimal(Long.toString(millis));
        switch (convertTo) {
            case 10:
                bd = bd.divide(new BigDecimal("10"));
            case 9:
                bd = bd.divide(new BigDecimal("10"));
            case 8:
                bd = bd.divide(new BigDecimal("10"));
            case 7:
                bd = bd.divide(new BigDecimal("365.2422"));
            case 6:
                bd = bd.divide(new BigDecimal("24"));
            case 5:
                bd = bd.divide(new BigDecimal("60"));
            case 4:
                bd = bd.divide(new BigDecimal("60"));
            case 3:
                bd = bd.divide(new BigDecimal("10"));
            case 2:
                bd = bd.divide(new BigDecimal("10"));
            case 1:
                bd = bd.divide(new BigDecimal("10"));
            case 0:
                break;
            default:
                throw new IllegalArgumentException("Illegal Time Converter Index: " + convertTo);
        }
        return bd;
    }

    public String getAttendanceCodes(int i) {
        return getAttendanceCodes().get(i);
    }

    public Time getAttendanceStartTime() {
        return attendanceStartTime;
    }

    public MasterSchedule getTodaysSchedule() {
        return null;
    }

    public String getStartScreenTimeFormat() {
        return startScreenTimeFormat;
    }

    /**
     * @param MAX_ATTENDANCE_EXTRA_TIME the MAX_ATTENDANCE_EXTRA_TIME to set
     */
    public void setMAX_ATTENDANCE_EXTRA_TIME(int MAX_ATTENDANCE_EXTRA_TIME) {
        this.MAX_ATTENDANCE_EXTRA_TIME = MAX_ATTENDANCE_EXTRA_TIME;
    }

    /**
     * @return the attendanceCodes
     */
    public ArrayList<String> getAttendanceCodes() {
        return attendanceCodes;
    }

    /**
     * @param attendanceCodes the attendanceCodes to set
     */
    public void setAttendanceCodes(ArrayList<String> attendanceCodes) {
        this.attendanceCodes = attendanceCodes;
    }

    /**
     * @param attendanceStartTime the attendanceStartTime to set
     */
    public void setAttendanceStartTime(Time attendanceStartTime) {
        this.attendanceStartTime = attendanceStartTime;
    }

    public static String colorToHex(Color color) {
            return String.format( "#%02X%02X%02X",
                    (int)( color.getRed() * 255 ),
                    (int)( color.getGreen() * 255 ),
                    (int)( color.getBlue() * 255 ));
    }

    public static Color textFill(Color background) {
        return textFill(background, 1.5);
    }

    public static Color textFill(Color background, double threshold) {
        return background.getRed() + background.getGreen() + background.getBlue() > threshold ? Color.BLACK : Color.WHITE;
    }

    public static void fireMouse(Node n, EventType<MouseEvent> type) {
        n.fireEvent(new MouseEvent(type, 0,0,0,0,null, 0, false, false,false, false, false, false, false, false, false, false, null));
    }

    public static Color highlightColor(Color c) {
        if (c.equals(Color.BLACK))
            return Color.web("#505050");
        else return textFill(c).equals(Color.WHITE) ? c.brighter() : c.darker();
    }

    public static class Filler extends Region {
        public Filler() {
            HBox.setHgrow(this, Priority.ALWAYS);
        }
    }
}
