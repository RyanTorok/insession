package localserver;

public class ConnectionTest extends AnonymousCommand {

    public ConnectionTest(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() {
        return "success";
    }
}
