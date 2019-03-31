package localserver;

import localserver.database.QueryGate;

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
        if (!results.isBeforeFirst())
            return "error : invalid post id";
        while (results.isBeforeFirst())
            results.next();
        return "error : not implemented";
    }
}
