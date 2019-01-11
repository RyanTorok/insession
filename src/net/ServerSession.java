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
import java.util.Arrays;
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
        super("localhost", PORT);
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
            if (!command(this, "authenticate", username, encoded)) {
                return false;
            }
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            setErrorMsg("error : security exception occurred");
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
            System.out.println(tempId);
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
        open = false;
        command(this,"close");
        oneTimeKey = "";
        closeSocket();
    }

    private synchronized boolean command(ServerSession serverSession, String name, String... arguments) {
        if (!open && !name.equals("authenticate"))
            return false;
        name = escape(name);
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = escape(arguments[i]);
        }
        long id = serverSession instanceof AnonymousServerSession ? 0 : User.active() == null ? 0 : User.active().getUniqueID();
        if (id == 0)
            id = tempId;
        StringBuilder cmd = new StringBuilder(name + " " + oneTimeKey + " " + id);
        for (String s : arguments) {
            cmd.append(" ").append(s);
        }
        writer.println(escape(cmd.toString()));
        try {
            if (name.equals("authenticate"))
                return true;
            oneTimeKey = reader.readLine().trim();
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

    public boolean sendOnly(String name, String... arguments) {
        try {
            return command(this, name, arguments) && reader.readLine().trim().equals("done");
        } catch (IOException e) {
            return false;
        }
    }

    public String[] callAndResponse(String name, String... arguments) {
        return callAndResponseInner(name, arguments).split(" ");
    }

    private String callAndResponseInner(String name, String... arguments) {
        if(!command(this, name, arguments)) {
            String s = "error : call exception occurred";
            setErrorMsg(s);
            return s;
        }
        try {
            String result = URLDecoder.decode(reader.readLine(), StandardCharsets.UTF_8);
            System.out.println("here888");
            if (isError(result)) {
                setErrorMsg(result);
            }
            System.out.println(result);
            return result;
        } catch (IOException e) {
            String s = "error : response exception occurred";
            setErrorMsg(s);
            return s;
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

    public static boolean isError(String[] results) {
        return isError(results[0]);
    }

    protected static boolean isError(String s) {
        int space = s.indexOf(" ");
        return s.substring(0, space < 0 ? s.length() : space).equals("error");
    }

    protected final void setOpen(boolean open) {
        this.open = open;
    }
}