package main;

import classes.SQL;
import db.SQLLocal;
import gui.Main;
import javafx.scene.paint.Color;

import java.net.*;
import java.sql.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 11ryt on 4/14/2017.
 */

public class Root {

    private static Integer activeID;
    private static Main activeFrame;
    private static UtilAndConstants utilAndConstants;
    private static User active = null;
    private static Main portal;
    private static String macAddress;


    public static void main(String[] args) {
        String startupError = null;
        /*try {
           // SQL.connect();
        } catch (OfflineException e) {
            //TODO setup offline setup edge case handle
            e.printStackTrace();
        }*/
        //utilAndConstants = SQL.initUtilAndConstants();
        try {
            macAddress = searchForMACAddress();
        } catch (Exception e) {
            startupError = e.getMessage();
        }
        //TODO intialize active user
        active = new Student();
        active.setBirthday(new Date(0));
        active.setAccentColor(new Color(218/255.0, 213/255.0, 237/255.0, 1));

        portal.main(new String[]{startupError});
    }

    public static String searchForMACAddress() throws SocketException {
        String firstInterface = null;
        Map<String, String> addressByNetwork = new HashMap<>();
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface network = networkInterfaces.nextElement();

            byte[] bmac = network.getHardwareAddress();
            if (bmac != null) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < bmac.length; i++) {
                    sb.append(String.format("%02X%s", bmac[i], (i < bmac.length - 1) ? "-" : ""));
                }

                if (sb.toString().isEmpty() == false) {
                    addressByNetwork.put(network.getName(), sb.toString());
                }

                if (sb.toString().isEmpty() == false && firstInterface == null) {
                    firstInterface = network.getName();
                }
            }
        }

        if (firstInterface != null) {
            return addressByNetwork.get(firstInterface);
        }

        return null;
    }

    public static Integer getActiveID() {
        return activeID;
    }

    public static javafx.application.Application getActiveFrame() {
        return activeFrame;
    }

    public static UtilAndConstants getUtilAndConstants() {
        return utilAndConstants;
    }

    public static Main getPortal() {
        return portal;
    }

    public static User getActiveUser() {
        return active;
    }

    public static String getMACAddress() {
        return macAddress;
    }

    public static void setMACAddress(String macAddress) {
        Root.macAddress = macAddress;
    }
}
