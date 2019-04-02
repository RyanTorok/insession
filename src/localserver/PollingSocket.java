package localserver;

import java.io.*;
import java.net.Socket;

public class PollingSocket extends CentralServerSession {
    private static final int PORT = 6523;
    private static final String HOST = "localhost";
    //TODO there seems to be a race condition which deadlocks the central server when this is set to 0.
    private final int DELAY_MILLIS = 250;


    public PollingSocket() throws IOException {
        this(HOST);
    }

    protected PollingSocket(String host) throws IOException {
        super(host, PORT);
        open();
        //irrelevant, just used for testing when server and client are run on the same JVM
        setEnableProgressBar(false);
    }

    public String[] poll() {
        String[] result;
        do {
            try {
                Thread.sleep(DELAY_MILLIS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result = callAndResponse("poll");
            if (isError(result)) {
                System.err.println(getErrorMsg());
                return new String[0];
            }
        } while (result.length == 1 && result[0].equals("done"));
        return result;

    }

}
