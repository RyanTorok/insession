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
            new QueryGate().query("INSERT INTO views (`post`, `user`) VALUES(?, ?);", "ul", getArgumentAsString(0), getArgumentAsString(1), new Timestamp(System.currentTimeMillis()));
            return "done";
        } catch (SQLException e) {
            return "error : database exception thrown";
        }
    }
}
