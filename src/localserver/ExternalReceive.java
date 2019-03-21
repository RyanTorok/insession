package localserver;

import java.io.IOException;
import java.sql.SQLException;

public class ExternalReceive extends AnonymousCommand {
    public ExternalReceive(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException {
        Long token = getArgumentAsLong(0);
        String receive = External.receive(token);
        if (receive == null) {
            return "error : receive timed out";
        }
        return receive;
    }
}
