package terminal;

import java.util.ArrayList;

public class HIDE extends Command {

    @Override
    protected TerminalRet execute(ArrayList<Token> tokens) throws TerminalException {
        assertTokenCount(1, tokens, "no arguments");
        return new TerminalRet("", true, false);
    }
}
