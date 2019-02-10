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
        while (results.isBeforeFirst()) {
            results.next();
        }
        if (results.isAfterLast())
            return "error : no entry exists";
        String serfile = results.getString("serfile");
        //remove + signs for pass over URL
        serfile = serfile.replaceAll("\\+", "#");
        if (serfile == null)
            return "error : no file exists";
        return serfile;
    }
}
