package server;

import server.database.QueryGate;

import java.sql.ResultSet;
import java.sql.SQLException;

public class HostExists extends Command {
    public HostExists(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException, SQLException {
        String nickname = getArgumentAsString(0);
        ResultSet resultSet = new QueryGate().query("SELECT COUNT(`id`) from registered_hosts WHERE nickname = ?", "s", nickname);
        return Boolean.toString(!resultSet.isAfterLast());
    }
}
