package net;

import java.io.IOException;

public class AnonymousServerSession extends ServerSession {

    public AnonymousServerSession() throws IOException {
        super();
    }

    @Override
    public boolean open() {
        System.out.println("here");
        getWriter().println("anonymous");
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
            System.out.println("connection error!");
            setErrorMsg("error : connection error");
            return false;
        }
    }

    @Override
    public void close() throws IOException {

        setOneTimeKey("");
        closeSocket();
    }
}
