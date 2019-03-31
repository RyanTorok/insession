package server;

import localserver.IDAllocator;
import server.database.QueryGate;
import main.PasswordManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

public class RegisterHost extends AnonymousCommand {

    public RegisterHost(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException, SQLException {
        long id = IDAllocator.getLong();
        String nickname = getArgumentAsString(0);
        String ipv4 = getArgumentAsString(1);
        String ipv6 = getArgumentAsString(2);
        int version = getArgumentAsInteger(3);
        String localEncryptedPassword = getArgumentAsString(4);
        String authToken = getArgumentAsString(5);
        int tokenLen = authToken.length();

        if (tokenLen == 0 && version != 0) {
            return "error : unauthorized creation of login-supporting host";
        }

        QueryGate queryGate = new QueryGate();
        if (tokenLen > 0) {
            //verify authorization token
            ResultSet s1 = queryGate.query("SELECT 1 FROM authorization_tokens WHERE token = ?;", "s", authToken);
            if (!s1.isBeforeFirst())
                return "error : authorization token not recognized";
        }

        //un-base64 the password
        localEncryptedPassword = new String(Base64.getDecoder().decode(localEncryptedPassword));

        PasswordManager.PasswordCombo combo = PasswordManager.newGen(localEncryptedPassword);
        if (combo == null) {
            return "error : security exception occurred";
        }

        //check if nickname exists
        ResultSet s = queryGate.query("SELECT 1 FROM registered_hosts WHERE `nickname` = ?", "s", nickname);
        if (s.isBeforeFirst())
            return "error : a host with this nickname exists";
        queryGate.update("INSERT INTO registered_hosts VALUE (?, ?, ?, ?, ?, ?, ?);", "lssslss", id, nickname, ipv4, ipv6, version, Base64.getEncoder().encodeToString(combo.getEncryptedPassword()), Base64.getEncoder().encodeToString(combo.getSalt()));
        return Long.toString(id);
    }
}
