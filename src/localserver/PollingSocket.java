package localserver;

import java.io.*;
import java.net.Socket;

public class PollingSocket extends Socket {
    private static final int PORT = 6521;
    private static final String HOST = "localhost";

    private BufferedReader reader;
    private PrintWriter writer;

    public PollingSocket() throws IOException {
        this(HOST);
    }

    protected PollingSocket(String host) throws IOException {
        super(host, PORT);
        reader = new BufferedReader(new InputStreamReader(this.getInputStream()));
        writer = new PrintWriter(this.getOutputStream(), true);
    }

    public String[] poll() {
        try {
            String s = reader.readLine();
            if (s == null)
                return null;
            return s.split("\\s+");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PrintWriter getWriter() {
        return writer;
    }
}
