package server;

import server.database.QueryGate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PreviousPost extends Command {
    public PreviousPost(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException, SQLException {
        UUID id = UUID.fromString(getArgumentAsString(0));
        Integer iterations = getArgumentAsInteger(1);
        QueryGate gate = new QueryGate();
        for (int i = 0; i < iterations; i++) {
            ResultSet u = gate.query("SELECT previous_version FROM posts WHERE uuid = ?", "u", id);
            while (u.isBeforeFirst())
                u.next();
            id = UUID.fromString(u.getString("previous_version"));
        }
        GetPost subCommand = new GetPost(new String[]{id.toString()});
        return subCommand.execute();
    }
}
