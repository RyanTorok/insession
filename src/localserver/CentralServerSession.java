package localserver;

import net.ServerSession;

import java.io.IOException;

public class CentralServerSession extends ServerSession {
    public static final int PORT = 6521;

    public CentralServerSession() throws IOException {
        //TODO replace with actual server host
        super("localhost", PORT);
    }
}
