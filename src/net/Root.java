package net;

import main.PasswordManager;
import main.User;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

public class Root {

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
                main.Root.getActiveUser().setPassword(newCombo.getEncryptedPassword());
                main.Root.getActiveUser().setPasswordSalt(newCombo.getSalt());
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

    public static UserMaybe login(String username, String password, boolean needSerFile) {
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
                UserMaybe user = loginSingle(username, urlEncode(PasswordManager.encrypt(password, saltBytes)), needSerFile);
                if (user.getExistsCode() == 2) {
                    assert user.getUser() != null;
                }
                return user;
            }
            return new UserMaybe(0, null, -1);
        } catch (IOException | ClassCastException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return new UserMaybe(-1, null, -1);
        }
    }

    private static UserMaybe loginSingle(String username, String password, boolean needSerFile) {
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
                return new UserMaybe(0, null, -1);
            }
            String[] split = in.split("\\s+", 2);
            long uniqueID = Long.parseLong(split[0]);
            readDone.close();
            if (in.contains("Connection failed")) {
                return new UserMaybe(-1, null, -1);
            }
            if (split[1].contains("true")) {
                if (needSerFile) {
                    return new UserMaybe(-2, null, uniqueID);
                }
                return new UserMaybe(1, null, uniqueID);
            }
            if (in.length() == 0)
                return new UserMaybe(0, null, uniqueID);
            if (needSerFile) {
                //generate user from object file
                byte[] serBytes = Base64.getDecoder().decode(split[1]);
                User fromSrc = (User) new ObjectInputStream(new ByteArrayInputStream(serBytes)).readObject();
                return new UserMaybe(2, fromSrc, uniqueID);
            } else {
                System.err.println("login error: not true after requesting no ser file.");
                return new UserMaybe(0, null, -1);
            }
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            e.printStackTrace();
            return new UserMaybe(-1, null, -1);
        }
    }

    public static boolean syncSerFileUp() {
        try {
            UserMaybe checkAccount = loginSingle(main.Root.getActiveUser().getUsername(), urlEncode(main.Root.getActiveUser().getPassword()), false);
            if (checkAccount.getExistsCode() == 1) {
                //user is valid
                byte[] serFileBytes = main.Root.getActiveUser().getSerFileBytes();
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
            UserMaybe checkAccount = loginSingle(main.Root.getActiveUser().getUsername(), urlEncode(main.Root.getActiveUser().getPassword()), false);
            if (checkAccount.getExistsCode() == 2) {
                return checkAccount.getUser();
            } else return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private static String urlEncode(byte[] bytes) throws UnsupportedEncodingException {
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
