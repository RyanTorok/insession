package db;

import main.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by 11ryt on 7/19/2017.
 */
public class SQLMaster {

    private static Connection overallServerConnection = null;

    public static void connectToOverallServer() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/paintbrush_server";
        String username = "java_server_master";
        String password = "Vzg7PvdMZeZg76p1f%";
        overallServerConnection = DriverManager.getConnection(url, username, password);
    }

    public static String getInstitutionURL(String nextLine) throws SQLException {
        if (overallServerConnection == null)
            connectToOverallServer();
        PreparedStatement ps = overallServerConnection.prepareStatement("Select `localdbaddress` from `clients` where `authenticationKey` = ?");
        ps.setString(1, nextLine);
        ResultSet rs = ps.executeQuery();
        String urlBase = rs.getString("localdbaddress");
        return urlBase + "?authenticationKey=" + nextLine + "&sessionID=null";
    }

    public static void createNewClient(String name) throws SQLException {
        if (overallServerConnection == null)
            connectToOverallServer();

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
        return 0;
    }

    public static User login(String username, String password) throws LoginException {
        String query = "SELECT * FROM users WHERE `username` = ? AND `password` = ?;";
        try {
            PreparedStatement statement = overallServerConnection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, PasswordManager.hash(password));
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
                case 0: newUser = new Student(Root.getMACAddress(), username, password, first, null, last, email, new Timestamp(System.currentTimeMillis()), null, -1);
                    break;
                case 1: newUser = new Teacher(Root.getMACAddress(), username, password, first, null, last, email, null, new Timestamp(System.currentTimeMillis()));
                    break;
                case 2: newUser = new Administrator(Root.getMACAddress(), username, password, first, null, last, email, null, new Timestamp(System.currentTimeMillis()));
                    break;
            }
            return newUser;
        } catch (SQLException e) {
            throw new LoginException(false);
        }
    }

    public static ArrayList lookUpActivationKey(String text) {
        return null;
    }
}