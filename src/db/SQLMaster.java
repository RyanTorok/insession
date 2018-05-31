package db;

import classes.School;
import main.Internet;

import java.sql.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by 11ryt on 7/19/2017.
 */
public class SQLMaster {

    private static Connection overallServerConnection = null;

    public static void connectToOverallServer() {
        String url = "jdbc:mysql://localhost:3306/paintbrush_server";
        String username = "java_server_master";
        String password = "Vzg7PvdMZeZg76p1f%";
        try {
            overallServerConnection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Cannot connect the database!", e);
        }
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

    }

    public static ArrayList lookUpActivationKey(String text) {
        return null;
    }
}