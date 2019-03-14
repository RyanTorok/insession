package server;

import java.math.BigInteger;
import java.sql.SQLException;

public class DHReq extends Command {
    public DHReq(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException, SQLException {
        Long token = getArgumentAsLong(0);
        String targetServerName = getArgumentAsString(1);
        BigInteger publicAN;
        try {
            publicAN = new BigInteger(getArgumentAsString(2));
        } catch (NumberFormatException e) {
            return "error : illegal publicAN format";
        }
        Long targetServerID = 0L; // get from name
        DHTable.Pair publicVars = DHTable.getPublicVars(token);
        Poll.request(targetServerID, "dhreq", token, publicVars.n, publicVars.g, publicAN);
        return "done";
    }
}
