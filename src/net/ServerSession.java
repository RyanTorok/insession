package net;

import main.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ServerSession extends Socket {

    public static final int PORT = 6520;

    private BufferedReader reader;
    private PrintWriter writer;
    private String oneTimeKey;
    private String errorMsg;
    private boolean open = false;

    public ServerSession() throws IOException {
        super(Net.ROOT_URL, PORT);
        try {
            reader = new BufferedReader(new InputStreamReader(this.getInputStream()));
            writer = new PrintWriter(this.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean open() {
        if (!command(this, "authenticate")) {
            return false;
        }
        String nonce = null;
        try {
            nonce = reader.readLine();
            if (isError(nonce)) {
                setErrorMsg(nonce);
                return false;
            }
        } catch (IOException e) {
            errorMsg = "Unable to access the server";
            return false;
        }
        if (nonce.length() == 0) {
            errorMsg = "Authentication failed";
            return false;
        }
        oneTimeKey = nonce;
        open = true;
        return true;
    }

    public void close() throws IOException {
        super.close();
        open = false;
        command(this, "close");
        oneTimeKey = "";
    }

    private synchronized boolean command(ServerSession serverSession, String name, String... arguments) {
        if (!open && !name.equals("authenticate") && !name.equals("close"))
            return false;
        name = escape(name);
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = escape(arguments[i]);
        }
        StringBuilder cmd = new StringBuilder(name + " " + oneTimeKey + " " + escape(User.active().getUsername()));
        for (String s : arguments) {
            cmd.append(" ").append(s);
        }
        writer.println(cmd);
        if (name.equals("authenticate"))
            return true;
        try {
            oneTimeKey = reader.readLine().trim();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                close();
            } catch (IOException e1) {
                return false;
            }
        }
        return true;
    }

    public boolean sendOnly(String name, String... arguments) {
        try {
            return command(this, name, arguments) && reader.readLine().trim().equals("done");
        } catch (IOException e) {
            return false;
        }
    }

    public String callAndResponse(String name, String... arguments) {
        if(!command(this, name, arguments)) {
            return "error : call exception occurred";
        }
        try {
            return reader.readLine();
        } catch (IOException e) {
            return "error : response exception occurred";
        }
    }

    private String escape(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    public final String getErrorMsg() {
        return errorMsg;
    }

    protected final void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    protected final void setOneTimeKey(String oneTimeKey) {
        this.oneTimeKey = oneTimeKey;
    }

    protected final String getOneTimeKey() {
        return oneTimeKey;
    }

    protected final BufferedReader getReader() {
        return reader;
    }

    protected final PrintWriter getWriter() {
        return writer;
    }

    protected static boolean isError(String s) {
        return s.substring(0, s.indexOf(" ")).equals("error");
    }

    protected final void setOpen(boolean open) {
        this.open = open;
    }
}