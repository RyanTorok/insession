package localserver;

import java.io.IOException;
import java.sql.SQLException;

public class TestExternal extends Command {
    public TestExternal(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException, SQLException {
        String nickname = getArgumentAsString(0);
        ExternalCall call = new ExternalCall(nickname);
        try {
            call.open();
            call.send("connectiontest");
            return call.receive();
        } catch (IOException e) {
            e.printStackTrace();
            return "failure";
        }
    }
}
