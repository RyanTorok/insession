package main;

import gui.Main;
import javafx.application.Application;
import net.ServerSession;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by 11ryt on 4/14/2017.
 */

public class Root {

    private static UtilAndConstants utilAndConstants;
    private static Main portal;
    private static String macAddress;
    private static Long activeID;
    private static Size size;


    public static void main(String[] args) {
        String startupError = null;
        try {
            macAddress = searchForMACAddress();
        } catch (Exception e) {
            startupError = e.getMessage();
        }

        utilAndConstants = new UtilAndConstants();
        size = new Size();

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

    public static Long getActiveID() {
        return activeID;
    }

    public static UtilAndConstants getUtilAndConstants() {
        return utilAndConstants;
    }

    public static Main getPortal() {
        return portal;
    }

    public static String getMACAddress() {
        return macAddress;
    }

    public static void setMACAddress(String macAddress) {
        Root.macAddress = macAddress;
    }

    public static Future<Boolean> saveAll() {
        Callable<Boolean> function = () -> {
            boolean success = false;
            if (User.active() != null && User.active().getUsername() != null) {
                User.active().write();
                success = syncSerFileUp();
            }
            DefaultUser def = new DefaultUser();
            def.read();
            def.write();
            return success;
        };
        ExecutorService execution = Executors.newSingleThreadExecutor();
        return execution.submit(function);
    }

    private static boolean syncSerFileUp() {
        //don't send the server our password or location
        byte[] temp = User.active().getPassword();
        int zipcode = User.active().getZipcode();
        if (temp == null)
            return false;
        User.active().setPassword(null);
        User.active().setZipcode(0);

        //encode the object file
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] bytes = null;
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(User.active());
            oos.flush();
            bytes = bos.toByteArray();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        String encoding = Base64.getEncoder().encodeToString(bytes);
        ServerSession session = null;
        boolean success = false;
        try {
            session = new ServerSession();
            boolean open = session.open(User.active().getUsername(), new String(temp));
            if (open) {
                success = session.sendOnly("setserfile", encoding);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        User.active().setPassword(temp);
        User.active().setZipcode(zipcode);
        return success;
    }

    public static void setPortal(Main main) {
        portal = main;
    }

    public static void setUtilAndConstants(UtilAndConstants utilAndConstants) {
        Root.utilAndConstants = utilAndConstants;
    }

    public static Size sizeInstance() {
        return size;
    }
}
