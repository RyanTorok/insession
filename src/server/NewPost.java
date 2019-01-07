package server;

import server.database.QueryGate;

import java.sql.SQLException;
import java.sql.Timestamp;

public class NewPost extends Command {

    public NewPost(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException {
        try {
            new QueryGate().query("INSERT INTO posts VALUE" + allQuestionMarks("posts", true) + ";", "ulusssttssusi",
                    IDAllocator.get(),
                    getArgumentAsLong(0),
                    getArgumentAsString(1),
                    getArgumentAsString(2),
                    getArgumentAsString(3),
                    getArgumentAsString(4),
                    new Timestamp(System.currentTimeMillis()),
                    new Timestamp(System.currentTimeMillis()),
                    getArgumentAsString(5),
                    getArgumentAsString(6),
                    getArgumentAsString(7),
                    getArgumentAsString(8),
                    getArgumentAsInteger(9));
        } catch (SQLException e) {
            return "error : database exception thrown";
        }
        return null;
    }
}
