package server;

import server.database.QueryGate;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PrintSchedule extends Command {

    public PrintSchedule(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException {
        Long userId = getArgumentAsLong(0);
        QueryGate gate = new QueryGate();
        try {
            ResultSet results = gate.query("SELECT schedule.period, courses.num_periods, courses.term, classes.semester, schedule.class, courses.long_name, CONCAT(users.first, ' ', users.last) AS teacher FROM schedule INNER JOIN classes ON schedule.class=classes.uuid INNER JOIN courses ON classes.course=courses.uuid INNER JOIN users on classes.teacher=users.id WHERE schedule.student = ? ORDER BY period ASC;", Long.toString(userId));
            return makeReturn(results);
        } catch (SQLException e) {
            return "error : database exception thrown";
        }
    }
}
