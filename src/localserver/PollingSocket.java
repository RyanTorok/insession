package localserver;

import java.io.*;
import java.net.Socket;

public class PollingSocket extends CentralServerSession {
    private static final int PORT = 6523;
    private static final String HOST = "localhost";
    private final int DELAY_MILLIS = 3000;


    public PollingSocket() throws IOException {
        this(HOST);
        open();
    }

    protected PollingSocket(String host) throws IOException {
        super(host, PORT);
    }

    public String[] poll() {
        System.out.println("polling socket poll! Tra la laaaa!");
        String[] result;
        do {
            try {
                Thread.sleep(DELAY_MILLIS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result = callAndResponse("poll");
        } while (result.length == 1 && result[0].equals("done"));
        return result;

    }

}
