package localserver;

import localserver.database.QueryGate;
import main.PasswordManager;

import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Base64;

public class ChangePassword extends Command {
    public ChangePassword(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws SQLException {
        Long id = getExecutorId();
        String base64Pwd = URLDecoder.decode(getArgumentAsString(0));
        byte[] localEncryptedPwd = Base64.getDecoder().decode(base64Pwd);
        PasswordManager.PasswordCombo combo = PasswordManager.newGen(new String(localEncryptedPwd));
        if (combo == null) {
            return "error : security exception thrown";
        }
        String serverEncryptedPwd = Base64.getEncoder().encodeToString(combo.getEncryptedPassword());
        String serverSalt = Base64.getEncoder().encodeToString(combo.getSalt());
        new QueryGate().update("UPDATE users SET password = ?, salt = ? WHERE id = ?", "ssl", serverEncryptedPwd, serverSalt, id);
        return "done";
    }
}
