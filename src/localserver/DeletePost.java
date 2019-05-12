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
            ResultSet previousVersion = gate.query("SELECT HEX(previous_version) as prev FROM posts WHERE uuid = ? AND poster = ?", "ul", id, getExecutorId());
            while (previousVersion.isBeforeFirst())
                previousVersion.next();
            if (previousVersion.isAfterLast())
                return "error : nonexistent post or not authorized";
            //in case the result set is altered by removing its row
            final String previous_version = previousVersion.getString("prev");
            UUID temp = previous_version == null ? null : uuidNoDashes(previous_version);
            gate.update("DELETE FROM posts WHERE uuid = ? AND poster = ?", "ul", id.toString(), getExecutorId().toString());
            id = temp;
        }
        return "done";
    }
}