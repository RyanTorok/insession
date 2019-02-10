package server;


import java.sql.SQLException;

public abstract class AnonymousCommand extends Command {
    public AnonymousCommand(String[] arguments) {
        super(arguments);
    }

    public String executeWithPermissions() throws WrongArgumentTypeException, SQLException {
        return execute();
    }
}
