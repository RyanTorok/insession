package server;

import localserver.IDAllocator;
import server.database.QueryGate;
import main.PasswordManager;

import java.sql.SQLException;

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
        PasswordManager.PasswordCombo combo = PasswordManager.newGen(localEncryptedPassword);
        if (combo == null) {
            return "error : security exception occurred";
        }
        new QueryGate().update("INSERT INTO registered_hosts VALUE (?, ?, ?, ?, ?, ?, ?);", "lssslss", id, nickname, ipv4, ipv6, version, combo.getEncryptedPassword(), combo.getSalt());
        return Long.toString(id);
    }
}
