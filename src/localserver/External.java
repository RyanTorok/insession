package localserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class External extends AnonymousCommand {

    private static HashMap<Long, ExternalCall> existingSessions = ExternalCall.activeCalls;

    public External(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() {
        String nickname = getArgumentAsString(0);
        ExternalCall externalCall = new ExternalCall(nickname);
        try {
            externalCall.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Random random = new Random();
        long token = random.nextLong();
        //TODO this is bad form - basically a spin lock.
        while (existingSessions.containsKey(token)) {
            token = random.nextLong();
        }
        existingSessions.put(token, externalCall);
        return Long.toString(token);
    }

    public static void sendMessage(Long token, String message) throws IOException {
        ExternalCall call = existingSessions.get(token);
        if (call != null)
            call.sendMessage(message);
    }

    public static void sendCommand(Long token, String message) throws IOException {
        ExternalCall call = existingSessions.get(token);
        if (call != null)
            call.sendCommand(message);
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
