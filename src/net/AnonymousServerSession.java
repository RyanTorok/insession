package net;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AnonymousServerSession extends ServerSession {

    public AnonymousServerSession() throws IOException {
        super();
    }

    public AnonymousServerSession(String host, int port) throws IOException {
        super(host, port);
    }

    @Override
    public boolean open() {
        writeText("anonymous");
        try {
            String oneTimeKey = getReader().readLine();
            if (isError(oneTimeKey)) {
                setErrorMsg(oneTimeKey);
                return false;
            }
            setOneTimeKey(oneTimeKey);
            setOpen(true);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("connection error!");
            setErrorMsg("error : connection error");
            return false;
        }
    }
}
