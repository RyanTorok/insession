package localserver;

import localserver.database.QueryGate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DeletePost extends Command {
    public DeletePost(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException, SQLException {
        UUID id = getArgumentAsUUID(0);

        QueryGate gate = new QueryGate();

        while (id != null) {
            ResultSet previousVersion = gate.query("SELECT previous_verison FROM posts WHERE uuid = ? AND poster = ?", "ul", id, getExecutorId());
            while (previousVersion.isBeforeFirst())
                previousVersion.next();
            if (previousVersion.isAfterLast())
                return "error : nonexistent post or not authorized";
            gate.update("DELETE FROM posts WHERE uuid = ? AND poster = ?", id.toString(), getExecutorId().toString());
            id = UUID.fromString(previousVersion.getString("previous_version"));
            if (previousVersion.wasNull())
                id = null;
        }
        return "done";
    }
}