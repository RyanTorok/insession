package localserver;

import localserver.database.QueryGate;

import java.sql.SQLException;

public class SetSerFile extends Command {

    public SetSerFile(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws SQLException {
        String encoded = getArgumentAsString(0);
        new QueryGate().update("UPDATE users SET serfile = ? WHERE id = ? ;", "sl", encoded, getExecutorId());
        return "done";
    }
}
