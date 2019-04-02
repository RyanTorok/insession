package localserver;

import java.io.IOException;

public class ExternalSend extends AnonymousCommand {
    public ExternalSend(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException {
        Long token = getArgumentAsLong(0);
        boolean command = getArgumentAsBoolean(1);
        try {
            if (command) External.sendCommand(token, getArgumentAsString(2));
            else External.sendMessage(token, getArgumentAsString(2));
            return "done";
        } catch (IOException e) {
            e.printStackTrace();
            return "error : send exception occurred";
        }
    }
}
