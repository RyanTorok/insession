package main;

import classes.MasterSchedule;
import classes.School;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

/**
 * Created by 11ryt on 7/3/2017.
 */
public class UtilAndConstants {

    private School school;

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

    public UtilAndConstants() {

    }

    private static void initOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win"))
            setOperatingSystem("win");
        else if (os.contains("linux"))
            setOperatingSystem("linux");
        else if (os.contains("mac"))
            setOperatingSystem("mac");
    }

    public static String parseTimestamp(Timestamp timestamp) {
        long then = timestamp.getTime();
        long now = System.currentTimeMillis();
        BigDecimal daysAgo_ = convertTimeUnits(now - then, (byte) 6);
        double daysAgo = daysAgo_.doubleValue();
        if (daysAgo < 7) {
            //less than one week
            if (daysAgo < 1) {
                //less than one day
                double hoursAgo = daysAgo * 24;
                if (hoursAgo < 1) {
                    //less than one hour
                    double minutesAgo = 60 * hoursAgo;
                    if (minutesAgo < 1) {
                        //less than one minute
                        return ((int) (60 * minutesAgo)) + pluralizeTimeUnits(" second", (int) (60 * minutesAgo)) + " ago";
                    } else return ((int) minutesAgo) + pluralizeTimeUnits(" minute", (int) minutesAgo) + " ago";
                } else return ((int) hoursAgo) + pluralizeTimeUnits(" hour", (int) hoursAgo) + " ago";
            } else return ((int) daysAgo) + pluralizeTimeUnits(" day", (int) daysAgo) + " ago";
        } else {
            SimpleDateFormat year = new SimpleDateFormat("YYYY");
            String thenYear = year.format(new Date(then)), nowYear = year.format(new Date(now));
            if (thenYear.equals(nowYear))
                return new SimpleDateFormat("MMMMMMMMM dd").format(then);
            return new SimpleDateFormat("M/d/YYYY").format(then);
        }
    }

    private static String pluralizeTimeUnits(String singular, int count) {
        if (count != 1)
            return singular + "s";
        else return singular;
    }

    public static String parseTimeNanos(long nanos) {
        if (nanos < 1000)
            return nanos + " ns";
        if (nanos < 1000000)
            return nanos / 1000 + " " + (char) 0x03bc + "s";
        if (nanos < 1000000000)
            return nanos / 1000000 + " ms";
        return nanos / 1000000000 + " sec";
    }

    public static Date date(LocalDate localDate) {
        if (localDate == null)
            return new Date(System.currentTimeMillis());
        Instant instant = Instant.from(localDate.atStartOfDay(ZoneId.systemDefault()));
        return Date.from(instant);
    }

    public static String parseLargeNumber(long num) {
        if (num < 1E4) return Long.toString(num);
        if (num < 1E6) return num / 1000 + "K";
        if (num < 1E9) return new DecimalFormat("%.1f").format((double) num / 1000000.0) + "M";
        return new DecimalFormat("%.1f").format((double) num / 1000000000.0) + "B";
    }

    public int getMAX_ATTENDANCE_EXTRA_TIME() {
        return MAX_ATTENDANCE_EXTRA_TIME;
    }

    public static long elapsedTimeMillis(Time earlierTime, Time laterTime) {
        return laterTime.getTime() - earlierTime.getTime();
    }

    public static BigDecimal elapsedTimeUnits(Time earlierTime, Time laterTime, byte convertTo) {
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

    private static BigDecimal convertTimeUnits(long millis, byte convertTo) {
        BigDecimal bd = new BigDecimal(Long.toString(millis));
        switch (convertTo) {
            case 10:
                bd = bd.divide(new BigDecimal("10"), 25, RoundingMode.HALF_UP);
            case 9:
                bd = bd.divide(new BigDecimal("10"), 25, RoundingMode.HALF_UP);
            case 8:
                bd = bd.divide(new BigDecimal("10"), 25, RoundingMode.HALF_UP);
            case 7:
                bd = bd.divide(new BigDecimal("365.2422"), 25, RoundingMode.HALF_UP);
            case 6:
                bd = bd.divide(new BigDecimal("24"), 25, RoundingMode.HALF_UP);
            case 5:
                bd = bd.divide(new BigDecimal("60"), 25, RoundingMode.HALF_UP);
            case 4:
                bd = bd.divide(new BigDecimal("60"), 25, RoundingMode.HALF_UP);
            case 3:
                bd = bd.divide(new BigDecimal("10"), 25, RoundingMode.HALF_UP);
            case 2:
                bd = bd.divide(new BigDecimal("10"), 25, RoundingMode.HALF_UP);
            case 1:
                bd = bd.divide(new BigDecimal("10"), 25, RoundingMode.HALF_UP);
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

    public static String[] parsePHPDataOutBase64(String base64, int expectedLength) {
        byte[] actualBytes = Base64.getDecoder().decode(base64.getBytes(StandardCharsets.UTF_8));
        String string = new String(actualBytes);
        String split[] = new String[expectedLength];
        for (int i = 0; i < split.length; i++) {
            split[i] = "";
        }
        int index = 0;
        int charAt = 0;
        boolean escaped = false;
        while (charAt < string.length()) {
            if (index > split.length)
                throw new IllegalArgumentException("Too many splits in post encoding");
            char c = string.charAt(charAt);
            if (escaped) {
                if (c == '\\')
                    split[index] += '\\';
                else if (c == ';')
                    split[index] += ';';
                else throw new IllegalArgumentException("Illegal escape sequence in decoded post: " + '\\' + c);
            } else {
                if (c == '\\')
                    escaped = true;
                else if (c == ';')
                    index++;
                else split[index] += c;
            }
            charAt++;
        }
        if (index <= string.length() - 1) {
            throw new IllegalArgumentException("Not enough splits in post encoding");
        }
        return split;
    }


    public static String ordinal(int i) {
        String[] sufixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + sufixes[i % 10];

        }
    }
}
