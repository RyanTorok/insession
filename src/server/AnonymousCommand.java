package server;

public abstract class AnonymousCommand extends Command {
    public AnonymousCommand(String[] arguments) {
        super(arguments);
    }

    public String executeWithPermissions() throws WrongArgumentTypeException {
        return execute();
    }

}
