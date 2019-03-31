package main;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class PasswordManager {

    public static String hash(String password) {
        return password;//TODO
    }

    public static String validate(String text) {
        if (text.length() < 6) {
            return "Your password must contain at least 6 characters.";
        }
        return "";
    }


    public static boolean attempt(String attemptedPassword, byte[] encryptedPassword, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        //encrypt the user's attempt using the user's unique salt
        byte[] encryptedAttempt = encrypt(attemptedPassword, salt);
        //return true if the encrypted password matches encrypted attempt

        //slow equals to prevent rate attacking
        int diff = encryptedAttempt.length ^ encryptedPassword.length;
        for(int i = 0; i < encryptedAttempt.length && i < encryptedPassword.length; i++)
            diff |= encryptedAttempt[i] ^ encryptedPassword[i];
        return diff == 0;
    }

    public static byte[] encryptWithLocalSalt(String password, String username) throws InvalidKeySpecException, NoSuchAlgorithmException {
        return encrypt(password, (username).getBytes());
    }

    public static byte[] encrypt(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String algorithm = "PBKDF2WithHmacSHA1";
        int derivedKeyLength = 160;

        int iterations = 25000;

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength);

        SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);

        return factory.generateSecret(spec).getEncoded();
    }

    public static byte[] createSalt() throws NoSuchAlgorithmException {
        SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[32];
        rand.nextBytes(salt);
        return salt;
    }

    public static PasswordCombo newGen(String password) {
        try {
            byte[] salt = createSalt();
            byte[] encrypted = encrypt(password, salt);
            return new PasswordCombo(encrypted, salt);
        } catch (GeneralSecurityException e) {
            return null;
        }
    }

    public static PasswordCombo newGenLocal(String password, String username)  {
        byte[] salt = username.getBytes(StandardCharsets.UTF_8);
        try {
            return new PasswordCombo(encrypt(password, salt), salt);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class PasswordCombo {
        byte[] encryptedPassword;
        byte[] salt;

        public byte[] getSalt() {
            return salt;
        }

        public void setSalt(byte[] salt) {
            this.salt = salt;
        }

        public byte[] getEncryptedPassword() {

            return encryptedPassword;
        }

        public void setEncryptedPassword(byte[] encryptedPassword) {
            this.encryptedPassword = encryptedPassword;
        }

        public PasswordCombo(byte[] encryptedPassword, byte[] salt) {

            this.encryptedPassword = encryptedPassword;
            this.salt = salt;
        }
    }
}