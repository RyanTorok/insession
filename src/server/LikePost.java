package server;

import server.database.QueryGate;

import java.sql.SQLException;
import java.sql.Timestamp;

public class LikePost extends Command {
    public LikePost(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException {
        try {
            new QueryGate().query("INSERT INTO likes (`post`, `user`) VALUES(?, ?);", "ul", getArgumentAsString(0), getArgumentAsString(1), new Timestamp(System.currentTimeMillis()));
            return "done";
        } catch (SQLException e) {
            return "error : database exception thrown";
        }
    }
}
