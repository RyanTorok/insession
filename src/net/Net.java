package net;

import main.PasswordManager;
import main.User;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

public class Net {

    public static final String ROOT_URL = "http://paintbrush.org/";

    public static URL root() throws MalformedURLException {
        return new URL(ROOT_URL);
    }

    public static boolean changePassword(String password, long uniqueID, String username, byte[] oldPassword) {
        try {
            PasswordManager.PasswordCombo newCombo = PasswordManager.newGen(password);

            //post data
            String data = "id="+ uniqueID + "&password=" + urlEncode(newCombo.getEncryptedPassword()) + "&salt=" + urlEncode(newCombo.getSalt()) + "&old=" + urlEncode(oldPassword) + "&username=" + username;
            byte[] postData = data.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;

            //start connection
            URL url = new URL(root(), "acct/changePassword.php");
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
            if (in.contains("done")) {
                User.active().setPassword(newCombo.getEncryptedPassword());
                User.active().setPasswordSalt(newCombo.getSalt());
                return true;
            } else {
                System.out.println(in);
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

    public static CreateAccountStatus createAccount(String username, String password, String first, String last, String email, String schoolCode) {
        try {
            PasswordManager.PasswordCombo passwordHash = PasswordManager.newGen(password);
            //post data
            String data = "username=" + username + "&password=" + urlEncode(passwordHash.getEncryptedPassword()) + "&salt=" + urlEncode(passwordHash.getSalt()) + "&first=" + first + "&last=" + last + "&email=" + email + "&schoolcode=" + ((schoolCode == null) ? "" : schoolCode);
            byte[] postData = data.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;

            //start connection
            URL url = new URL(root(), "acct/createAccount.php");
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
            String in = "", fullText = "";
            while (in != null) {
                in = readDone.readLine();
                if (in != null)
                    fullText += in + "\n";
            }
            readDone.close();
            if (fullText.contains("badSC")) {
                return new CreateAccountStatus(-3, null);
            }
            if (fullText.contains("Connection failed")) {
                return new CreateAccountStatus(-2, null);
            }
            if (fullText.contains("done") || fullText.contains("student")) {
                return new CreateAccountStatus(1, passwordHash);
            }
            if (fullText.contains("teacher")) {
                return new CreateAccountStatus(2, passwordHash);
            }
            if (fullText.contains("administrator")) {
                return new CreateAccountStatus(3, passwordHash);
            }
        } catch (IOException e) {
            return new CreateAccountStatus(-2, null);
        }
        return new CreateAccountStatus(-2, null);
    }

    public static URL makeURL(String... names) {
        try {
            return new URL(new URL(ROOT_URL), Arrays.stream(names).map(directory -> directory + "/").collect(Collectors.joining()));
        } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
        }
    }

    public static long getOnlineTime() {
        try {
            URL url = makeURL("util", "time.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(0));
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            BufferedReader readTime = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            long time = Long.parseLong(readTime.readLine().trim());
            readTime.close();
            return time;
        } catch (IOException | NumberFormatException e) {
            return -1;
        }
    }


    public static class CreateAccountStatus {
        int status;
        PasswordManager.PasswordCombo passwordCombo;

        public CreateAccountStatus(int status, PasswordManager.PasswordCombo passwordCombo) {
            this.status = status;
            this.passwordCombo = passwordCombo;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public PasswordManager.PasswordCombo getPasswordCombo() {
            return passwordCombo;
        }

        public void setPasswordCombo(PasswordManager.PasswordCombo passwordCombo) {
            this.passwordCombo = passwordCombo;
        }
    }


    public static boolean syncSerFileUp() {
        try {
            UserMaybe checkAccount = Login.loginSingle(User.active().getUsername(), urlEncode(User.active().getPassword()), false);
            if (checkAccount.getExistsCode() == 1) {
                //user is valid
                byte[] serFileBytes = User.active().getSerFileBytes();
                if (serFileBytes == null)
                    return false;
                new ThreadedCall<Boolean>("acct/updateSerFile.php", true, new PostRequest("serfile", urlEncode(serFileBytes))).procedureCall((list) -> list.contains("done"), (b) -> {});
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static User syncSerFileDown() {
        try {
            UserMaybe checkAccount = Login.loginSingle(User.active().getUsername(), urlEncode(User.active().getPassword()), false);
            if (checkAccount.getExistsCode() == 2) {
                return checkAccount.getUser();
            } else return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    static String urlEncode(byte[] bytes) throws UnsupportedEncodingException {
       return URLEncoder.encode(new String(Base64.getEncoder().encode(bytes)), StandardCharsets.UTF_8);
    }

    public static class UserMaybe {
        private int existsCode;
        private long uniqueID;

        public long getUniqueID() {
            return uniqueID;
        }

        public void setUniqueID(long uniqueID) {
            this.uniqueID = uniqueID;
        }

        private User user;

        public UserMaybe(int existsCode, User user, long uniqueID) {
            this.uniqueID = uniqueID;
            this.setExistsCode(existsCode);
            this.setUser(user);
        }

        public int getExistsCode() {
            return existsCode;
        }

        public void setExistsCode(int existsCode) {
            this.existsCode = existsCode;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }
}
