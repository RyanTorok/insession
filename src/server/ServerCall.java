package server;

import net.Login;
import net.Net;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class ServerCall {

    private static final HashMap<Long, List<SessionToken>> oneTimeKeys = new HashMap<>();

    String[] arguments;

    ServerCall(String command) {
        String[] args = command.split(" ");
        for (int i = 0; i < args.length; i++) {
            args[i] = URLDecoder.decode(args[i], StandardCharsets.UTF_8);
        }
        arguments = args;
    }

    public String execute() {
        if (arguments.length < 3)
            return "error : too few arguments";
        String opcode = arguments[0],
                oneTimeKey = arguments[1],
                userId = arguments[2];

        //special case for session start
        if (opcode.equals("authenticate")) {
            long userID = authenticate(arguments[1], arguments[2]);
            if (userID >= 0) {
                String key = ServerMain.keyGen();
                //we can set the permissions to privileged because we authenticate anyway
                oneTimeKeys.put(userID, new ArrayList<>(Collections.singleton(new SessionToken(key, false))));
                return key;
            }
            return "error : authentication failure";
        }

        //check the provided userId - OTK pair
        SessionToken newKey = quickAuthenticate(Long.parseLong(userId), oneTimeKey);

        if (newKey == null)
            return "error : authentication failure";

        try {
            Command command = Command.getAsType(arguments[0], arguments);
            if (command != null) {
                if (!(command instanceof AnonymousCommand) && newKey.anonymous)
                    return newKey + "\nerror : insufficient permissions for command '" + arguments[0] + "'";
                if (command instanceof AnonymousCommand && !newKey.anonymous)
                    return newKey + "\n" + ((AnonymousCommand) command).executeWithPermissions();
                return newKey + "\n" + command.execute();
            } else return newKey + "\nerror : unknown command: " + arguments[0];
        } catch (WrongArgumentTypeException e) {
            return newKey + "\nerror : " + e.getMessage();
        }
    }

    private long authenticate(String username, String hashedClientPassword) {
        Net.UserMaybe user = Login.login(username, hashedClientPassword, false);
        if (user.getExistsCode() == 1) return user.getUniqueID();
        else return -1;
    }

    private synchronized SessionToken quickAuthenticate(long userID, String oneTimeKey) {
        List<SessionToken> keys = oneTimeKeys.get(userID);
        if (keys == null)
            return null;
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i).oneTimeKey.equals(oneTimeKey)) {
                String newKey = ServerMain.keyGen();
                SessionToken token = new SessionToken(newKey, keys.get(i).anonymous);
                keys.set(i, token);
                return token;
            }
        }
        return null;
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
