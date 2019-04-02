package server;

import server.database.QueryGate;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Message extends Command {
    public Message(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException, SQLException {
        String targetNickname = getArgumentAsString(0);
        Long serverID = getServerID(targetNickname);
        if (serverID == null)
            return "error : unknown target host";
        long token = getArgumentAsLong(1);
        String commandName = getArgumentAsString(2);
        String arguments = getArgumentAsString(3);
        Poll.request(serverID, -1 * token, getExecutorNickname(), commandName, arguments);
        return "done";
    }
}
