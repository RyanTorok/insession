package server;

import server.Command;
import server.database.QueryGate;

import java.sql.SQLException;

public class MergeClass extends Command {

    public MergeClass(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() {
        QueryGate gate = new QueryGate();
        try {
            gate.query("INSERT INTO merged_classes (`source`, `destination`) VALUES (?, ?);", getArgumentAsString(0), getArgumentAsString(1));
            return "done";
        } catch (SQLException e) {
            return "error : database exception thrown";
        }
    }
}
