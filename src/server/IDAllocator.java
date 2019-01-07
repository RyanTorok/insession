package server;

import main.Root;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.UUID;


public class IDAllocator {

    /*public static UUID get() {
        //48 bits
        byte[] macBytes = Root.getMACAddress().getBytes();
        ByteBuffer longStream = ByteBuffer.wrap(macBytes);

        //format: reverse(MAC address with 16 random bits appended) << 64 | reverse(nanoTime)
        return new UUID(Long.reverse(longStream.getLong() << 16 | new Random(0xffff).nextInt()), Long.reverse(System.nanoTime()));
    }*/
    public static UUID get() {
        return UUID.randomUUID();
    }

    public static long getLong() {
        UUID uuid = get();
        return uuid.getLeastSignificantBits() ^ uuid.getMostSignificantBits();
    }
}