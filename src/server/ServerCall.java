package server;

import main.PasswordManager;
import org.json.JSONObject;
import server.database.QueryGate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;

public class ServerCall {

    private static final HashMap<Long, HashSet<SessionToken>> oneTimeKeys = new HashMap<>();

    String[] arguments;
    private boolean requestedClose;
    private boolean closableWithoutAuth;
    private boolean pollsOnly;

    ServerCall(String command, boolean closableWithoutAuth, boolean pollsOnly) {
        this.pollsOnly = pollsOnly;
        command = new String(Base64.getDecoder().decode(command), StandardCharsets.UTF_8);
        arguments = command.split(" ");
        //de-escape all the command arguments, but not the command name or one time key
        for (int i = 3; i < arguments.length; i++) {
            arguments[i] = URLDecoder.decode(arguments[i], StandardCharsets.UTF_8);
        }
        this.closableWithoutAuth = closableWithoutAuth;
        requestedClose = false;
    }

    public String execute() {
        //System.out.println("central: " + Arrays.toString(arguments));
        if (arguments.length < 1)
            return "error : too few arguments";

        String opcode = arguments[0];

        //special case for session start
        if (opcode.equals("authenticate")) {
            long userID;
            String username = arguments[3];
            if (arguments[4].length() < 2)
                return "error : authentication failure";
            byte[] password = Base64.getDecoder().decode(arguments[4]);
            String hashedClientPassword = new String(password, StandardCharsets.UTF_8);
            userID = authenticate(username, hashedClientPassword);
            if (userID >= 0) {
                String key = ServerMain.keyGen();
                //we can set the permissions to privileged because we authenticate anyway
                SessionToken o = new SessionToken(key, false);
                HashSet<SessionToken> tokens = oneTimeKeys.computeIfAbsent(userID, k -> new HashSet<>());
                tokens.add(o);
                closableWithoutAuth = false;
                return key + "\n" + userID;
            }
            return "error : authentication failure";
        }

        //these anonymous commands are safe and fast, we don't really have to prevent the polling module accessing these.
        // There's no reason to use it this way, but we won't fail if they do.
        if (opcode.equals("anonymous")) {
            String key = ServerMain.keyGen();
            HashSet<SessionToken> tokens = oneTimeKeys.computeIfAbsent(0L, k -> new HashSet<>());
            tokens.add(new SessionToken(key, true));
            return key;
        }

        if (opcode.equals("connectiontest")) {
            requestedClose = true;
            return "success";
        }

        if (closableWithoutAuth && opcode.equals("close")) {
            requestedClose = true;
            return "done";
        }

        if (opcode.equals("serverdetails")) {
            JSONObject result = LocalServers.lookup(arguments[3]);
            return  "null\n" + (result != null ? result.toString() : "error : unrecognized or invalid domain name");
        }

        if (opcode.equals("registerhost")) {
            try {
                return new RegisterHost(arguments).execute();
            } catch (WrongArgumentTypeException e) {
                e.printStackTrace();
                return "error : argument error occured";
            } catch (SQLException e) {
                e.printStackTrace();
                return "error : database error occured";
            }
        }

        if (opcode.equals("close")) {
            if (closableWithoutAuth) {
                requestedClose = true;
                return "done";
            }
        }

        if (arguments.length < 3)
            return "error : too few arguments";
         String oneTimeKey = arguments[1],
                userId = arguments[2];

        //check the provided userId - OTK pair
        long userID = Long.parseLong(userId);
        SessionToken newToken = quickAuthenticate(userID, oneTimeKey);
        if (newToken == null) {
            return "error : authentication failure";
        }

        String newKey = newToken.oneTimeKey;

        try {
            Command command = Command.getAsType(arguments[0], arguments, userID);
            if (command instanceof Close) {
                requestedClose = true;
            }
            if (pollsOnly && !(command instanceof Poll)) {
                return "error : tried to execute normal command on polling socket";
            }
            if (command != null) {
                if (!(command instanceof AnonymousCommand) && newToken.anonymous)
                    return newKey + "\nerror : insufficient permissions for command '" + arguments[0] + "'";
                if (command instanceof AnonymousCommand && !newToken.anonymous)
                    return newKey + "\n" + ((AnonymousCommand) command).executeWithPermissions();
                return newKey + "\n" + command.execute();
            } else return newKey + "\nerror : unknown command: " + arguments[0];
        } catch (WrongArgumentTypeException e) {
            return newKey + "\nerror : " + e.getMessage();
        } catch (SQLException e) {
            e.printStackTrace();
            return newKey + "\nerror : database exception thrown";
        }
    }

    private long authenticate(String hostname, String hashedClientPassword) {
        try {
            ResultSet results = new QueryGate().query("SELECT id, `password`, salt FROM registered_hosts WHERE nickname = ?", "s", hostname);
            boolean attempt = false;
            long id = -1;
            while (!attempt && !results.isAfterLast()) {
                while (results.isBeforeFirst())
                    results.next();
                byte[] serverPwd = Base64.getDecoder().decode(results.getString(  "password"));
                byte[] salt = Base64.getDecoder().decode(results.getString("salt"));
                attempt = PasswordManager.attempt(hashedClientPassword, serverPwd, salt);
                id = results.getLong("id");
                results.next();
            }
            return attempt ? id : -1;
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            return -1;
        }
    }

    private synchronized SessionToken quickAuthenticate(long userID, String oneTimeKey) {
        HashSet<SessionToken> keys = oneTimeKeys.get(userID);
        if (keys == null)
            return null;
        synchronized (oneTimeKeys.get(userID)) {
            boolean exists = keys.removeIf(sessionToken -> oneTimeKey.equals(sessionToken.oneTimeKey));
            if (exists) {
                String newKey = ServerMain.keyGen();
                SessionToken token = new SessionToken(newKey, userID == 0);
                keys.add(token);
                return token;
            }
        }
        return null;
    }

    //TODO: not thread safe
    boolean requestedClose() {
        return requestedClose;
    }

    public boolean isClosableWithoutAuth() {
        return closableWithoutAuth;
    }

    private static class SessionToken {
        String oneTimeKey;
        boolean anonymous;

        SessionToken(String key, boolean anonymous) {
            oneTimeKey = key;
            this.anonymous = anonymous;
        }
    }
}
