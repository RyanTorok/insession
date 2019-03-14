package net;

import main.PasswordManager;
import main.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class ServerSession extends Socket {

    public static final int PORT = 6520;

    private BufferedReader reader;
    private PrintWriter writer;
    private String oneTimeKey = "";
    private String errorMsg;
    private boolean open = false;
    private long tempId = 0;

    public ServerSession() throws IOException {
        this("localhost", PORT);
    }

    protected ServerSession(String host, int port) throws IOException {
        super(host, port);
        try {
            reader = new BufferedReader(new InputStreamReader(this.getInputStream()));
            writer = new PrintWriter(this.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean open() {
        return open(User.active().getUsername(), new String(User.active().getPassword()));
    }

    public boolean open(String username, String password) {
        try {
            byte[] src = PasswordManager.encryptWithLocalSalt(password, username);
            String encoded = Base64.getEncoder().encodeToString(src);
            if (!command("authenticate", username, encoded)) {
                return false;
            }
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            setErrorMsg("error : security exception occurred");
            return false;
        }
        String nonce = null;
        try {
            nonce = reader.readLine();
            if (nonce == null || isError(nonce)) {
                setErrorMsg(nonce);
                return false;
            }
        } catch (IOException e) {
            errorMsg = "error : unable to access the server";
            return false;
        }
        if (nonce.length() == 0) {
            errorMsg = "error : authentication failed";
            return false;
        }
        oneTimeKey = nonce;
        try {
            tempId = Long.parseLong(reader.readLine().trim());
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            tempId = 0;
        }
        open = true;
        return true;
    }

    protected void closeSocket() throws IOException {
        super.close();
    }

    public void close() throws IOException {
        command("close");
        oneTimeKey = "";
        closeSocket();
    }

    synchronized boolean command(String name, String... arguments) {
        if (!open && !name.equals("authenticate"))
            return false;
        if (name.equals("close")) {
            open = false;
        }
        name = escape(name);
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = escape(arguments[i]);
        }
        long id = this instanceof AnonymousServerSession ? 0 : User.active() == null ? 0 : User.active().getUniqueID();
        if (id == 0)
            id = tempId;
        StringBuilder cmd = new StringBuilder(name + " " + oneTimeKey + " " + id);
        for (String s : arguments) {
            cmd.append(" ").append(s);
        }
        writeText(cmd.toString());
        try {
            if (name.equals("authenticate"))
                return true;
            String s = reader.readLine();
            if (s == null) {
                /*
                    If this was a close command, this is completely normal, since the read fails if the socket dies.
                    Otherwise, something went horribly wrong on the server.
                */
                return false;
            }
            oneTimeKey = s.trim();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            try {
                close();
            } catch (IOException ignored) {
            }
            return false;
        }
    }

    protected void writeText(String cmd) {
        writer.println(escape(cmd));
    }

    public boolean sendOnly(String name, String... arguments) {
        try {

            boolean success = command(name, arguments);
            if (!success)
                return name.equals("close");
            String result = reader.readLine().trim();
            if (isError(result))
                setErrorMsg(result);
            return result.equals("done");
        } catch (IOException e) {
            return false;
        }
    }

    public String[] callAndResponse(String name, String... arguments) {
        String s = callAndResponseInner(name, arguments);
        if (s == null)
            return null;
        return s.split(" ");
    }

    private String callAndResponseInner(String name, String... arguments) {
        if(!command(name, arguments)) {
            String s = "error : call exception occurred";
            setErrorMsg(s);
            return s;
        }
        try {
            String result = URLDecoder.decode(reader.readLine(), StandardCharsets.UTF_8);
            if (isError(result)) {
                setErrorMsg(result);
                return null;
            }
            return result;
        } catch (IOException e) {
            String s = "error : response exception occurred";
            setErrorMsg(s);
            return s;
        }
    }

    protected String escape(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    public final String getErrorMsg() {
        return errorMsg;
    }

    public final String getTruncatedErrorMsg() {
        String orig = getErrorMsg();
        return orig.substring(orig.indexOf(":") + 1);
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

    public static boolean isError(String[] results) {
        if (results == null)
            return true;
        if (results.length == 0)
            return false;
        return isError(results[0]);
    }


    protected static boolean isError(String s) {
        int space = s.indexOf(" ");
        return s.substring(0, space < 0 ? s.length() : space).equals("error");
    }

    protected final void setOpen(boolean open) {
        this.open = open;
    }

    public boolean connectionTest() {
        try {
            writeText("connectiontest");
            String result = getReader().readLine().trim();
            return result.equals("success");
        } catch (IOException e) {
            return false;
        }
    }

}