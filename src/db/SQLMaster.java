package db;

import main.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by 11ryt on 7/19/2017.
 */
public class SQLMaster {

    public static Connection connectToOverallServer() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/paintbrush_server";
        String username = "java_server_master";
        String password = "Vzg7PvdMZeZg76p1f%";
        return DriverManager.getConnection(url, username, password);
    }

    public static String getInstitutionURL(String nextLine) throws SQLException {
        Connection overallServerConnection = connectToOverallServer();
        PreparedStatement ps = overallServerConnection.prepareStatement("Select `localdbaddress` from `clients` where `authenticationKey` = ?");
        ps.setString(1, nextLine);
        ResultSet rs = ps.executeQuery();
        String urlBase = rs.getString("localdbaddress");
        return urlBase + "?authenticationKey=" + nextLine + "&sessionID=null";
    }

    public static void createNewClient(String name) throws SQLException {
        Connection overallServerConnection = connectToOverallServer();
        //generate random authentication key
        StringBuffer keySB = new StringBuffer();
        String availableChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        availableChars += availableChars.toLowerCase() + "1234567890";
        for (int i = 0; i < 255; i++) {
            Random random = new Random();
            random.setSeed((long) (Math.random() * Long.MAX_VALUE));
            keySB.append(availableChars.charAt(random.nextInt(availableChars.length())));
        }
        PreparedStatement ps = overallServerConnection.prepareStatement("Insert into `client_assoc` (`name`, `authenticationKey`, `localdbaddress`) values " +
                "(?, ?, ?);");
        ps.setString(1, name);
        ps.setString(2, keySB.toString());
        ps.setString(3, Internet.URL_Root + "/lookup/" + name + ".php");
        ps.execute();
    }

    public static int createAccount(String username, String password, String first, String last, String email, String schoolCode) {
        try (Connection conn = connectToOverallServer()){
            String verifyUN = "SELECT 1 FROM users WHERE  `username` = ?";
            PreparedStatement ps = conn.prepareStatement(verifyUN);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return -1; //username already in use
            }
            int type = 0;
            if (schoolCode != null && schoolCode.length() > 0) {
                String getUType = "";
                ps = conn.prepareStatement(getUType);
                ps.setString(1, schoolCode);
                rs = ps.executeQuery();
                if (!rs.next()) {
                    return -3; //no valid type due to invalid school code
                }
                type = rs.getInt("type");
            }
            String createAcct = "INSERT INTO users (`username`, `password`, `first`, `last`, `email`) values (?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(createAcct);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, first);
            ps.setString(4, last);
            ps.setString(5, email);
            boolean success = ps.execute();
            if (!success) {
                return -2; //server error
            }
            return type;
        } catch (SQLException e) {
            return -2; //represents connection error
        }
    }

    public static User login(String username, String password) throws LoginException {
        String query = "SELECT * FROM users WHERE `username` = ? AND `password` = ?;";
        try (Connection overallServerConnection = connectToOverallServer()) {
            PreparedStatement statement = overallServerConnection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
            overallServerConnection.close();
            if (!rs.next()) {
               throw new LoginException(false);
            }
            String first = rs.getString("first");
            String last = rs.getString("last");
            String email = rs.getString("email");
            String schoolCode = rs.getString("schoolcode");
            int type = rs.getInt("type");
            User newUser = null;
            switch (type) {
                case 0: newUser = new Student(Root.getMACAddress(), username, password.getBytes(), first, null, last, email, new Timestamp(System.currentTimeMillis()), null, -1);
                    break;
                case 1: newUser = new Teacher(Root.getMACAddress(), username, password.getBytes(), first, null, last, email, null, new Timestamp(System.currentTimeMillis()));
                    break;
                case 2: newUser = new Administrator(Root.getMACAddress(), username, password.getBytes(), first, null, last, email, null, new Timestamp(System.currentTimeMillis()));
                    break;
            }
            return newUser;
        } catch (SQLException e) {
            throw new LoginException(true);
        }
    }

    public static ArrayList lookUpActivationKey(String text) {
        return null;
    }

    public static boolean updatePassword(User activeUser, String password) {

        try (Connection conn = connectToOverallServer()){
            String query = "UPDATE users SET `password` = ? WHERE `username` = ?;";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, activeUser.getUsername());
            ps.setString(2, password);
            return ps.execute();
        } catch (SQLException e) {
            return false;
        }
    }
}