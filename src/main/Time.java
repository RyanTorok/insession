package main;

import net.Net;

public class Time {

    private static long lastValidOnlineTime;
    private static long offset;

    public static long currentSafeTime() {
        update();
        return fastSafeTime();
    }

    public static long fastSafeTime() {
        return System.currentTimeMillis() + offset;
    }

    private static void update() {
        long ctm = Net.getOnlineTime();
        if (ctm < 0) {
            //TODO handle when unable to get online time
            return;
        }
        lastValidOnlineTime = ctm;
        offset = lastValidOnlineTime - System.currentTimeMillis();
    }
}
