package net;

import org.json.JSONObject;

import java.io.IOException;


public class AnonymousCentralServerSession extends AnonymousServerSession {

    public static final String HOST = "localhost";
    public static final int PORT = 6521;

    public AnonymousCentralServerSession() throws IOException {
        super(HOST, PORT);
    }

    public JSONObject requestLocalServer(String nickname) {
        if (isClosed())
            open();
        boolean serverdetails = command("serverdetails", nickname);
        if (!serverdetails)
            return null;
        try {
            String result = getReader().readLine();
            if (isError(result))
                return null;
            return new JSONObject(result);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
