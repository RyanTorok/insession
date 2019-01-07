package net;

import java.io.IOException;

public class AnonymousServerSession extends ServerSession {

    public AnonymousServerSession() throws IOException {
        super();
    }

    @Override
    public boolean open() {
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
            setErrorMsg("error : connection error");
            return false;
        }
    }
}
