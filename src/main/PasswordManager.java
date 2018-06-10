package main;

import db.SQLMaster;

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

    public static String set(String password) {
        if (SQLMaster.updatePassword(Root.getActiveUser(), password)) {
            return password;
        } return null;
    }

    public static boolean attempt(String text, User u) {
        return text.equals(u.getPassword());
    }
}