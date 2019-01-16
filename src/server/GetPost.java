package server;

import server.database.QueryGate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class GetPost extends Command {
    public GetPost(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException, SQLException {
        UUID postID = getArgumentAsUUID(0);
        ResultSet results = new QueryGate().query("SELECT * FROM posts WHERE uuid = ?", "u", postID);
        while (results.isBeforeFirst())
            results.next();
        if (results.isAfterLast())
            return "error : invalid post id";
        return "error : not implemented";
    }
}
