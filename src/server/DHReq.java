package server;

import localserver.database.QueryGate;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DHReq extends Command {
    public DHReq(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException, SQLException {
        String targetNickname = getArgumentAsString(0);
        Long token = getArgumentAsLong(1);
        BigInteger publicAG;
        try {
            publicAG = new BigInteger(getArgumentAsString(2));
        } catch (NumberFormatException e) {
            return "error : illegal publicAG format";
        }
        long targetServerID = 0L;

        //get targetServerID from nickname
        ResultSet result = new QueryGate().query("SELECT `id` FROM registered_hosts WHERE nickname = ?;", "s", targetNickname);
        while (!result.isAfterLast()) {
            result.next();
            targetServerID = result.getLong("id");
        }
        if (targetServerID == 0L) {
            return "error : host does not exist";
        }
        DHTable.Pair publicVars = DHTable.getPublicVars(token);
        Poll.request(targetServerID, token, getExecutorNickname(), "dhreq", publicVars.n, publicVars.g, publicAG);
        return "done";
    }
}
