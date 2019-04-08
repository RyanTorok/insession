package net;

import gui.Main;
import main.PasswordManager;
import main.Root;
import main.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class ServerSession implements AutoCloseable {

    public static final int PORT = 6520;
    private static final int TIMEOUT_MILLIS = 100000;

    private BufferedReader reader;
    private PrintWriter writer;
    private String oneTimeKey;
    private String errorMsg;
    private boolean open;
    private long tempId;
    private boolean promptOnAuthenticationFailure;
    private boolean enableProgressBar;
    private Socket socket;

    public ServerSession() throws IOException {
        this("localhost", PORT);
    }

    protected ServerSession(String host, int port) throws IOException {
        //socket = SSLSocketFactory.getDefault().createSocket(host, port);
            socket = new Socket(host, port);
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            socket.setSoTimeout(TIMEOUT_MILLIS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        oneTimeKey = "";
        open = false;
        tempId = 0;
        promptOnAuthenticationFailure = false;
        setEnableProgressBar(true);
    }

    public boolean open() {
        return open(User.active().getUsername(), new String(User.active().getPassword()));
    }

    public boolean open(String username, String password) {
        try {
            byte[] src = PasswordManager.encryptWithLocalSalt(password, username);
            String encoded = Base64.getEncoder().encodeToString(src);
            if (!command("authenticate", username, encoded))
                return false;
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            setErrorMsg("error : security exception occurred");
            return false;
        }
        String nonce;
        try {
            nonce = reader.readLine();
            if (nonce == null || isError(nonce)) {
                setErrorMsg(nonce);
                if (promptOnAuthenticationFailure) {
                    password = Root.getPortal().promptLogin();
                    if (password == null)
                        return false;
                    return open(username, password);
                }
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
        socket.close();
    }

    public void close() throws IOException {
        if (!open)
            return;
        command("close");
    }

    synchronized boolean command(String name, String... arguments) {
        boolean isAuthenticate = name.equals("authenticate");
        if (!open && !isAuthenticate)
            return false;
        boolean close = name.equals("close");
        if (close) {
            open = false;
        }
        for (int i = 0; i < arguments.length; i++) {
        }
        long id = getEffectiveID();
        if (id == 0)
            id = tempId;
        StringBuilder cmd = new StringBuilder(name + " " + oneTimeKey + " " + id);
        for (String s : arguments) {
            cmd.append(" ").append(urlEncode(s));
        }
        writeText(cmd.toString());
        try {
            if (close) {
                closeSocket();
                oneTimeKey = "";
                return true;
            }
            if (isAuthenticate)
                return true;
            String s;
            try {
                s = reader.readLine();
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                setErrorMsg("error : read timed out");
                return false;
            }
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

    protected long getEffectiveID() {
        return this instanceof AnonymousServerSession ? 0 : User.active() == null ? 0 : User.active().getUniqueID();
    }

    protected void writeText(String cmd) {
        writer.println(escape(cmd));
    }

    public boolean sendOnly(String name, String... arguments) {
        String[] returnVal = callAndResponse(name, arguments);
        return returnVal.length == 1 && returnVal[0].equals("done");
    }

    public String[] callAndResponse(String name, String... arguments) {
        Main portal = Root.getPortal();
        if (portal != null && isEnableProgressBar())
            portal.progressBar(.5);
        String[] s = callAndResponseInner(name, arguments).split(" ");
        if (portal != null && isEnableProgressBar())
            portal.progressBar(1);
        return s;
    }

    private String callAndResponseInner(String name, String... arguments) {
        if(!command(name, arguments)) {
            String s = "error : call exception occurred";
            setErrorMsg(s);
            return s;
        }
        try {
            String s = reader.readLine();
            String result = URLDecoder.decode(s, StandardCharsets.UTF_8);
            if (isError(result)) {
                setErrorMsg(result);
                return errorMsg;
            }
            return result;
        } catch (SocketTimeoutException e) {
            String errorMsg = "error : read timed out";
            setErrorMsg(errorMsg);
            return errorMsg;
        } catch (IOException e) {
            String errorMsg = "error : response exception occurred";
            setErrorMsg(errorMsg);
            return errorMsg;
        }
    }

    protected String escape(String s) {
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    protected String urlEncode(String s) {
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
            e.printStackTrace();
            return false;
        }
    }


    public boolean isPromptOnAuthenticationFailure() {
        return promptOnAuthenticationFailure;
    }

    public void setPromptOnAuthenticationFailure(boolean promptOnAuthenticationFailure) {
        this.promptOnAuthenticationFailure = promptOnAuthenticationFailure;
    }

    public boolean isEnableProgressBar() {
        return enableProgressBar;
    }

    public void setEnableProgressBar(boolean enableProgressBar) {
        this.enableProgressBar = enableProgressBar;
    }

    protected long getTempId() {
        return tempId;
    }

    protected boolean isClosed() {
        return !open;
    }
}
