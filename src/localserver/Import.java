package localserver;

import localserver.database.QueryGate;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Import extends Command {
    public Import(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws SQLException {
        String host_nickname = getArgumentAsString(0);

        //we need to make sure the host name actually exists
        QueryGate queryGate = new QueryGate();

        //first see if someone else in our database already registered it. If yes, we know it's valid without asking the main server.
        ResultSet exists = queryGate.query("SELECT `host` from imports WHERE `host` = ?", "s", host_nickname);
        boolean hostExists = !exists.isAfterLast();
        //if not, we have to ask the main server.
        if (!hostExists) {
            try {
                CentralServerSession session = new CentralServerSession();
                String[] response = session.callAndResponse("hostexists", host_nickname);
                hostExists = Boolean.valueOf(response[0]);
            } catch (IOException e) {
                return "error : an error occurred connecting to the import manager";
            }
        }
        if (!hostExists)
            return "error : host nonexistent";
        queryGate.update("INSERT IGNORE INTO imports (`user`, `host`) VALUES (?, ?)", "ls", getExecutorId(), host_nickname);
        return "done";
    }
}
