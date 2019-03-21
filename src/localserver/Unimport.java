package localserver;

import localserver.database.QueryGate;

import java.sql.SQLException;

public class Unimport extends Command {
    public Unimport(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException, SQLException {
        new QueryGate().update("DELETE FROM imports WHERE `user` = ? and `host` = ?;", "ls", getExecutorId(), getArgumentAsString(0));
        return "done";
    }
}
