package server;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;


public class DHTable {

    public static final int BIT_LENGTH = 2048;


    public static class Pair {
        BigInteger n;
        BigInteger g;

        Pair(BigInteger n, BigInteger g) {
            this.n = n;
            this.g = g;
        }
    }

    static {
        publicVarTable = new HashMap<>();
    }

    private static HashMap<Long, Pair> publicVarTable;

    public static Pair getPublicVars(Long token) {
        return publicVarTable.get(token);
    }

    public static void remove(Long token) {
        publicVarTable.remove(token);
    }

    public static Pair newGen(Long token) {
        //token already exists, try again
        if (publicVarTable.containsKey(token))
            return null;
        SecureRandom rand = new SecureRandom();
        BigInteger n = new BigInteger(BIT_LENGTH, rand);
        BigInteger g = new BigInteger(BIT_LENGTH, rand);
        Pair value = new Pair(n, g);
        publicVarTable.put(token, value);
        return value;
    }
}
