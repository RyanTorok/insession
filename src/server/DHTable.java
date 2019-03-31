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
        //we have to set the highest order bit because otherwise the security interface complains about key being too short
        n = n.setBit(2047);
        BigInteger g = new BigInteger(BIT_LENGTH, rand);
        g = g.setBit(2047);
        Pair value = new Pair(n, g);
        publicVarTable.put(token, value);
        return value;
    }
}
