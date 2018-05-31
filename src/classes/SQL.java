/**
 * Created by 11ryt on 4/1/2017.
 */
package classes;


import classes.setbuilder.Condition;
import classes.setbuilder.Set;
import exceptions.DatabaseException;
import exceptions.ExpressionSyntaxException;
import main.*;

import java.sql.*;
import java.util.ArrayList;

public class SQL {

    static Connection conn;
    static User[] masterUserList;

    public static void main(String[] args) {
        connect();
    }

    public static void connect() {
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String url = "jdbc:odbc:Driver={Microsoft Access Driver (*.accdb)};DBQ=evelyn.accdb;";
        String username = "";
        String password = "";
        try {
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    public static classes.setbuilder.Set getSet(String setName) {
        String base = "SELECT `string` FROM " + Root.getActiveID() + "_sets WHERE `name` = ?;";
        PreparedStatement ps = null;
        String returnVal = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(base);
            ps.setString(1, setName);
            rs = ps.executeQuery();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                ps.close();
                return new classes.setbuilder.Set(setName, new Condition(rs.getString("string")));
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ExpressionSyntaxException e) {
                e.printStackTrace();
            }

        }
        return new Set();
    }

    public static void initMasterUserList() throws SQLException, DatabaseException {
        String sql = "SELECT * FROM users;";
        ArrayList<User> returnList = new ArrayList<>();
        ResultSet rs = null;
        rs = conn.prepareStatement(sql).executeQuery();
        while (rs.next()) {
            if (rs.getString("Usertype").equalsIgnoreCase("Student"))
            {} //returnList.add(new Student(rs.getInt("ID"), rs.getString("mac"), rs.getString("username"), rs.getString("password"), rs.getString("first"), rs.getString("middle"), rs.getString("last"), rs.getString("email"), rs.getString("homephone"), rs.getString("cellphone"), rs.getString("address"), rs.getString("schoolCode"), rs.getString("studentID"), rs.getTimestamp("timestamp")));
            else if (rs.getString("Usertype").equalsIgnoreCase("Teacher"))
                returnList.add(new Teacher(rs.getInt("ID"), rs.getString("mac"), rs.getString("username"), rs.getString("password"), rs.getString("first"), rs.getString("middle"), rs.getString("last"), rs.getString("email"), rs.getString("homephone"), rs.getString("cellphone"), rs.getString("address"), rs.getString("schoolCode"), rs.getString("studentID"), rs.getTimestamp("timestamp")));
            else if (rs.getString("Usertype").equalsIgnoreCase("Administrator"))
                returnList.add(new Administrator(rs.getInt("ID"), rs.getString("mac"), rs.getString("username"), rs.getString("password"), rs.getString("first"), rs.getString("middle"), rs.getString("last"), rs.getString("email"), rs.getString("homephone"), rs.getString("cellphone"), rs.getString("address"), rs.getString("schoolCode"), rs.getString("studentID"), rs.getTimestamp("timestamp")));
            else throw new DatabaseException("Unrecognized User Type: " + rs.getString("Usertype"));
        }
    }

    public static User[] getMasterUserList() {
        return masterUserList;
    }

    public static int countExistingSchedules() {
        return 0;
    }

    public static ArrayList updateServer() throws SQLException{

        return null;
    }
}