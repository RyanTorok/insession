package terminal;

import java.util.ArrayList;

public class PWD extends Command {
    @Override
    protected TerminalRet execute(ArrayList<Token> tokens) {
        return new TerminalRet(System.getProperty("user.dir"), false, false);
    }

}
