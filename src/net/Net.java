package net;

import main.PasswordManager;
import main.User;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

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
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
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
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            return new CreateAccountStatus(-2, null);
        }
        return new CreateAccountStatus(-2, null);
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
                String data = "id=" + checkAccount.getUniqueID() +"&serfile=" + urlEncode(serFileBytes);
                byte[] postData = data.getBytes(StandardCharsets.UTF_8);
                int postDataLength = postData.length;

                //start connection
                URL url = new URL(root(), "acct/updateSerFile.php");
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
                if (in.contains("done"))
                    return true;
                else {
                    while(in != null) {
                        System.out.println(in);
                        in = readDone.readLine();
                    }
                    return false;
                }
            } else
                return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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
       return URLEncoder.encode(new String(Base64.getEncoder().encode(bytes)), "UTF-8");
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
