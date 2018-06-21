package net;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class TestSaltPost {
    public static void main(String[] args) {
        byte[] salt_ = new byte[50];
        new SecureRandom().nextBytes(salt_);
        byte[] salt = java.util.Base64.getEncoder().encode(salt_);
        String orig = new String(salt);
        String saltStr = null;
        try {
            saltStr = URLEncoder.encode(orig, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println(orig);
        try {
            //post data
            String data = "salt=" + saltStr;
            byte[] postData = data.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;

            //start connection
            URL url = new URL("http://paintbrush.org/acct/SaltRepeat.php");
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
            System.out.println("\nfull text:");
            System.out.println(fullText);
            readDone.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String byteToString(byte[] bytes) {
        StringBuilder b = new StringBuilder();
        for (byte c : bytes) {
            b.append((char) (c >= 0 ? c : 256 + c));
        }
        return b.toString();
    }
}
