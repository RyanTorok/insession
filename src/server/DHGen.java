package server;

import java.sql.SQLException;
import java.util.Random;

public class DHGen extends Command {

    public DHGen(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException, SQLException {
        Random rand = new Random();
        DHTable.Pair p;
        long token;
        do {
            token = Math.abs(rand.nextLong());
            p = DHTable.newGen(token);
        } while (p == null);
        return p.n + " " + p.g + " " + token;
    }
}
