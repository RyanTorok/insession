package terminal;

import java.util.ArrayList;

public class QUIT extends Command {

    @Override
    protected TerminalRet execute(ArrayList<Token> tokens) {
        if (tokens.size() > 1) {
            return new TerminalRet("Wrong number of arguments for command 'clear': expected no arguments.",  false, false);
        }
        return new TerminalRet("", true, true);
    }
}
