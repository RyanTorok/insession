package terminal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class OPEN extends Command {
    @Override
    protected TerminalRet execute(ArrayList<Token> tokens) throws TerminalException {
        assertTokenCount(2, tokens, "file to open");
        File f = Address.parse(tokens.get(1).getTokenLabel(), true, false, false, true, true);
        Process p;
        Runtime cr = Runtime.getRuntime();
        try {
            p = cr.exec(f.getName());
        } catch (IOException e) {
            throw new TerminalException("open - an error occurred when opening file '" + f.getName() + "'.");
        }
        return new TerminalRet("", false, false);
    }
}
