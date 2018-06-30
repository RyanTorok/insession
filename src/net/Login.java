package net;

import javafx.concurrent.Task;
import main.PasswordManager;
import main.User;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;

import static net.Net.root;

public class Login extends Task {

    private String username;
    private String password;
    private boolean needSerFile;
    private Net.UserMaybe returnVal = null;

    public Login(String username, String password, boolean needSerFile) {
        this.username = username;
        this.password = password;
        this.setNeedSerFile(needSerFile);
    }

    public static Net.UserMaybe login(String username, String password, boolean needSerFile) {
        try {
            //post data
            String data = "username=" + username;
            byte[] postData = data.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;

            //start connection
            URL url = new URL(root(), "acct/getLocalSalt.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.write(postData);
            }
            ArrayList<String> salts = new ArrayList<>();
            BufferedReader readSalts = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String in = readSalts.readLine();
            if (in != null)
                in = in.trim();
            while (in != null) {
                if (in.length() > 0)
                    salts.add(in);
                in = readSalts.readLine();
                if (in != null)
                    in = in.trim();
            }
            readSalts.close();
            for (String salt: salts) {
                byte[] saltBytes = Base64.getDecoder().decode(salt);
                Net.UserMaybe user = loginSingle(username, Net.urlEncode(PasswordManager.encrypt(password, saltBytes)), needSerFile);
                if (user.getExistsCode() == 2) {
                    assert user.getUser() != null;
                }
                return user;
            }
            return new Net.UserMaybe(0, null, -1);
        } catch (IOException | ClassCastException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return new Net.UserMaybe(-1, null, -1);
        }
    }

    static Net.UserMaybe loginSingle(String username, String password, boolean needSerFile) {
        try {
            //post data
            String data = "username=" + username + "&password=" + password + "&needfile="+needSerFile;
            byte[] postData = data.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;

            //start connection
            URL url = new URL(root(), "acct/login.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.write(postData);
            }
            BufferedReader readDone = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String in = readDone.readLine();
            int maxCount = 10, i = 0;
            while (in == null && ++i < maxCount) {
                in = readDone.readLine();
            }
            if (in == null) {
                return new Net.UserMaybe(0, null, -1);
            }
            String[] split = in.split("\\s+", 2);
            long uniqueID = Long.parseLong(split[0]);
            readDone.close();
            if (in.contains("Connection failed")) {
                return new Net.UserMaybe(-1, null, -1);
            }
            if (split[1].contains("true")) {
                if (needSerFile) {
                    return new Net.UserMaybe(-2, null, uniqueID);
                }
                return new Net.UserMaybe(1, null, uniqueID);
            }
            if (in.length() == 0)
                return new Net.UserMaybe(0, null, uniqueID);
            if (needSerFile) {
                //generate user from object file
                byte[] serBytes = Base64.getDecoder().decode(split[1]);
                User fromSrc = (User) new ObjectInputStream(new ByteArrayInputStream(serBytes)).readObject();
                return new Net.UserMaybe(2, fromSrc, uniqueID);
            } else {
                System.err.println("login error: not true after requesting no ser file.");
                return new Net.UserMaybe(0, null, -1);
            }
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            e.printStackTrace();
            return new Net.UserMaybe(-1, null, -1);
        }
    }

    public boolean needsSerFile() {
        return needSerFile;
    }

    public void setNeedSerFile(boolean needSerFile) {
        this.needSerFile = needSerFile;
    }

    public Net.UserMaybe getReturnVal() {
        return returnVal;
    }

    public void setReturnVal(Net.UserMaybe returnVal) {
        this.returnVal = returnVal;
    }

    @Override
    protected Object call() throws Exception {
        return login(username, password, needSerFile);
    }
}
