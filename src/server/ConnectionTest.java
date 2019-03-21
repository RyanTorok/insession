package server;

import java.sql.SQLException;

public class ConnectionTest extends Command {

    public ConnectionTest(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws SQLException {
        String target = getArgumentAsString(0);
        if (target.equals(""))
            return "success";

        Long serverID = getServerID(target);
        if (serverID == null)
            return "error : nonexistent host";

        Poll.request(serverID, -1L, getExecutorNickname(), "connectiontest");
        return "done";
    }
}
