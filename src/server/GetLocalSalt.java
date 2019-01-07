package server;

import server.database.QueryGate;

import java.sql.ResultSet;
import java.sql.SQLException;

@Deprecated
public class GetLocalSalt extends AnonymousCommand {
    public GetLocalSalt(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException {
        try {
            final ResultSet results = new QueryGate().query("SELECT client_salt FROM users WHERE username = ?", getArgumentAsString(0));
            StringBuilder out = new StringBuilder();
            while (!results.isAfterLast()) {
                out.append(results.getString("client_salt")).append("\n");
            }
            if (out.length() == 0)
                return "error : no such user";
            return out.toString();
        } catch (SQLException e) {
            return "error : database exception thrown";
        }
    }
}
