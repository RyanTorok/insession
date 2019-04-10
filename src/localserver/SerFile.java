package localserver;

import localserver.database.QueryGate;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SerFile extends Command {

    public SerFile(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws SQLException {
        ResultSet results = new QueryGate().query("SELECT serfile FROM users WHERE id = ?", "l", getExecutorId());
        if (!results.isBeforeFirst())
            return "error : no entry exists";
        while (results.isBeforeFirst()) {
            results.next();
        }
        String serfile = results.getString("serfile");
        serfile = serfile.replaceAll("\\+", "#");
        if (serfile == null)
            return "error : no file exists";
        return serfile;
    }
}
