package modulesearch;

import java.math.BigInteger;

public class WordDimension {
    BigInteger frequency = new BigInteger("0");

    void increment() {
        frequency = frequency.add(BigInteger.ONE);
    }
}
