package localserver;

import net.ServerSession;

import java.io.IOException;
import java.util.Base64;

public class CentralServerSession extends ServerSession {
    public static final int PORT = 6521;

    public CentralServerSession() throws IOException {
        //TODO replace with actual server host
        super("localhost", PORT);
    }

    @Override
    public boolean open() {
        ServerInfo environment = ServerMain.getEnvironment();
        environment.setPassword("");
        open(environment.getNickname(), environment.getPassword());
        super.open();

        return false;
    }

    //TODO maybe remove for security reasons, really only for testing
    Long registerHost(String nickname, String ipv4, String ipv6, int version, byte[] encryptedPassword) {
        try {
            writeText("registerhost 0 0 " + nickname + " " + ipv4 + " " + ipv6 + " " + version + " " + Base64.getEncoder().encodeToString(encryptedPassword));
            String result = getReader().readLine().trim();
            return Long.parseLong(result);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
