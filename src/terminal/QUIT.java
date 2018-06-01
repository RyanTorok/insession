package terminal;

import java.util.ArrayList;

public class QUIT extends Command {

    @Override
    protected TerminalRet execute(ArrayList<Token> tokens) throws TerminalException {
        if (!(tokens.size() == 1 || tokens.size() == 2 && tokens.get(1).getType() == Token.Type.TAG)) {
            throw new TerminalException("invalid argument for command 'quit': " + tokens.get(1).getTokenLabel());
        }
        boolean isQuitAll = tokens.size() == 2 && tokens.get(1).getTokenLabel().equals("ALL");
        if (tokens.size() == 2 && !isQuitAll)
            throw new TerminalException("quit - unexpected tag: '" + tokens.get(1).getTokenLabel() + "'. Use \\a to quit Paintbrush.");
        return new TerminalRet("Bye", !isQuitAll, !isQuitAll);
    }
}
