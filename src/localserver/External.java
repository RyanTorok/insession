package localserver;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Random;

public class External extends AnonymousCommand {

    private static HashMap<Long, ExternalCall> existingSessions = new HashMap<>();

    public External(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException, SQLException {
        String nickname = getArgumentAsString(0);
        ExternalCall externalCall = new ExternalCall(nickname);
        try {
            externalCall.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Random random = new Random();
        long token = random.nextLong();
        //TODO this is bad form, fix it
        while (existingSessions.containsKey(token)) {
            token = random.nextLong();
        }
        existingSessions.put(token, externalCall);
        return Long.toString(token);
    }

    public static void send(Long token, String message) throws IOException {
        ExternalCall call = existingSessions.get(token);
        if (call != null)
            call.send(message);
    }

    public static String receive(Long token) {
        ExternalCall call = existingSessions.get(token);
        if (call != null) {
            return call.receive();
        }
        return null;
    }

    static void receiveEnqueue(Long token, String message) {
        ExternalCall call = existingSessions.get(token);
        if (call != null) {
            call.receiveEnqueue(message);
        }
    }
}
