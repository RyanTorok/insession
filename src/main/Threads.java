package main;

public class Threads {
    private static Thread netAccess;

    public static Thread getNetAccessThread() {
        if (netAccess == null) netAccess = new Thread();
        return netAccess;
    }

}
