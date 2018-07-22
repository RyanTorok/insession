package main;

import gui.Main;
import javafx.geometry.Insets;

import java.net.*;
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

        utilAndConstants = new UtilAndConstants();

        Main.main(new String[]{startupError});
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

                if (!sb.toString().isEmpty()) {
                    addressByNetwork.put(network.getName(), sb.toString());
                }

                if (!sb.toString().isEmpty() && firstInterface == null) {
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

    public static void saveAll() {
        if (Root.getActiveUser() != null && Root.getActiveUser().getUsername() != null) {
            Root.getActiveUser().write();
            net.Net.syncSerFileUp();
        }
        DefaultUser def = new DefaultUser();
        def.read();
        def.write();
    }

    public static void setActiveUser(User read) {
        active = read;
    }

    public static void setPortal(Main main) {
        portal = main;
    }

    public static double fontSize(int i) {
        return utilAndConstants.fontSize(i);
    }

    public static double width(double width) {
        return utilAndConstants.width(width);
    }

    public static double height(double height) {
        return utilAndConstants.height(height);
    }

    public static Insets insets(double i) {
        return insets(i, i);
    }

    public static Insets insets(double top, double right, double bottom, double left) {
        return new Insets(height(top), width(right), height(bottom), width(left));
    }

    public static Insets insets(double vertical, double horizontal) {
        double w = width(horizontal), h = height(vertical);
        return new Insets(h, w, h, w);
    }

    public static double lessWidthHeight(double i) {
        return Math.min(width(i), height(i));
    }
}
