package localserver;

import java.io.IOException;

public class ExternalSend extends AnonymousCommand {
    public ExternalSend(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException {
        Long token = getArgumentAsLong(0);
        try {
            External.send(token, getArgumentAsString(1));
            return "done";
        } catch (IOException e) {
            e.printStackTrace();
            return "error : send exception occurred";
        }
    }
}
