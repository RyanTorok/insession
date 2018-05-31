/**
 * Created by 11ryt on 4/1/2017.
 */
/*package db;

import classes.DayRegex;
import exceptions.OfflineException;
import main.Root;
import main.UnsupportedOSException;
import main.User;
import main.UtilAndConstants;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQL {

    static Connection conn;

    public static void connect() throws OfflineException {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            String url = "jdbc:derby://localhost:3306/myDB;create=false;";
            String username = "root";
            String password = "";
            conn = DriverManager.getConnection(url, username, password);
        }
        catch (Exception e) {
            if (e instanceof ClassNotFoundException){
                e.printStackTrace();
                return;
            }
            //database does not exist.
            //SQLInstitution.connect();
            String url = "jdbc:derby://localhost:3306/myDB;create=true;";
            String username = "root";
            String password = "5002MyrQklm";
            try {
                conn = DriverManager.getConnection(url, username, password);
                PreparedStatement ps = conn.prepareStatement("\\. sqlScripts/initLocal.sql");
                ps.execute();
            } catch (Exception e1) {
                e1.printStackTrace();
                return;
            }
            //connect();
        }
    }

    public static void disconnect() {
        try {
            conn.close();
        } catch (SQLException e) {
            conn = null;
        }
    }

    public static User getUser(String mac) throws SQLException {
        Statement s;
        s = conn.createStatement();
        ResultSet rs = s.executeQuery("SELECT * FROM Users WHERE `MAC` = '" + mac + "';");
        return castToUserOfType(rs);
    }

    public static void add(User user) throws SQLException, UnsupportedOSException {
        Statement s;
        s = conn.createStatement();
        String sql = "INSERT INTO Users (`MAC`, `Username`, `Password`, `First`, `Middle`, `Last`, `Email`, `HomePhone`, `CellPhone`, `Address`, `SchoolCode`, `StudentID`) VALUES ('" + Root.getMACAddress() + "', '" + user.getUsername() + "', '"
                + user.getPassword() + "', '" + user.getFirst() + "', '" + user.getMiddle() + "', '" + user.getLast() + "', '" + user.getEmail() + "', '" + user.getHomePhone() + "', '" + user.getCellphone() + "', '" + user.getSchoolCode() + "', '" + user.getId() + ");";
        ResultSet rs = s.executeQuery(sql);
    }

    public static User getUserFromParams(User temp) throws SQLException {
        PreparedStatement s;
        conn.setAutoCommit(false);
        s = conn.prepareStatement("SELECT * FROM Users WHERE `Username` = ?;");
        s.setString(1, temp.getUsername());
        ResultSet rs = s.executeQuery();
        conn.commit();
        conn.setAutoCommit(true);
        return castToUserOfType(rs);
    }

    private static User castToUserOfType(ResultSet rs) throws SQLException {
        String type = rs.getString("Usertype");
        User active;
        switch (type) {
            case "Student":
                active = new main.Student(rs.getInt("ID"), rs.getString("MAC"), rs.getString("Username"), rs.getString("Password"), rs.getString("First"), rs.getString("Middle"), rs.getString("Last"), rs.getString("Email"), rs.getString("HomePhone"), rs.getString("CellPhone"), rs.getString("Address"), rs.getString("SchoolCode"), rs.getString("StudentID"), rs.getTimestamp("Timestamp"));
                break;
            case "Teacher":
                active = new main.Teacher(rs.getInt("ID"), rs.getString("MAC"), rs.getString("Username"), rs.getString("Password"), rs.getString("First"), rs.getString("Middle"), rs.getString("Last"), rs.getString("Email"), rs.getString("HomePhone"), rs.getString("CellPhone"), rs.getString("Address"), rs.getString("SchoolCode"), rs.getString("StudentID"), rs.getTimestamp("Timestamp"));
                break;
            case "Administrator":
                active = new main.Administrator(rs.getInt("ID"), rs.getString("MAC"), rs.getString("Username"), rs.getString("Password"), rs.getString("First"), rs.getString("Middle"), rs.getString("Last"), rs.getString("Email"), rs.getString("HomePhone"), rs.getString("CellPhone"), rs.getString("Address"), rs.getString("SchoolCode"), rs.getString("StudentID"), rs.getTimestamp("Timestamp"));
                break;
            default:
                active = null;
        }
        return active;
    }

    public static boolean userExists(User u) {
        try {
            getUserFromParams(u);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static void addMacAddress(User temp, String mac) throws SQLException {
        Statement s;
        s = conn.createStatement();
        String sql = "UPDATE Users SET `MAC` = CONCAT(`MAC`, '_sp*" + mac + "') WHERE `Username` = '" + temp.getUsername() + "' AND `SchoolCode` = '" + temp.getSchoolCode() + "';";
        ResultSet rs = s.executeQuery(sql);
    }

    public static UtilAndConstants initUtilAndConstants() {
        return null;
    }

    public static void newSchedule(classes.MasterSchedule schedule, DayRegex regex) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn.setAutoCommit(false);
            ps = conn.prepareStatement("Insert into scheduleroot(ScheduleCode, DaySet) Values (?, ?);");
            ps.setString(1, schedule.getScheduleCode());
            ps.setString(2, regex.toString());
            ps.executeUpdate();
            conn.commit();
            ps = conn.prepareStatement("Select `ID` from scheduleroot order by `ID` desc limit 1");
            rs = ps.executeQuery();
            conn.commit();
            ps = conn.prepareStatement("Create table ? (`PeriodID` int(11) Not Null AUTO_INCREMENT, `StartTime` Time Not Null, `EndTime` Time Not Null, `NextPd` int Not Null, Primary Key (`PeriodID`))");
            rs.first();
            ps.setString(1, "scheduleformat" + rs.getInt("ID"));
            ps.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
*/