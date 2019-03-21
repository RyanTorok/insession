package localserver;

import localserver.database.QueryGate;

import java.sql.SQLException;
import java.sql.Timestamp;

public class ViewPost extends Command {
    public ViewPost(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() {
        try {
            new QueryGate().update("INSERT INTO views (`post`, `user`) VALUES(?, ?);", "ul", getArgumentAsString(0), getArgumentAsString(1));
            return "done";
        } catch (SQLException e) {
            return "error : database exception thrown";
        }
    }
}
