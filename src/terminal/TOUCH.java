package terminal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class TOUCH extends Command {
    @Override
    protected TerminalRet execute(ArrayList<Token> tokens) throws TerminalException {
        assertTokenCount(2, tokens, "new file name");
        File f = Address.parse(tokens.get(1).getTokenLabel(), false, true, false, false, false);
        try {
            boolean success = f.createNewFile();
            if (!success)
                throw new TerminalException("An error occurred creating " + f.getName() + ".");
        } catch (IOException e) {
            throw new TerminalException("An error occurred creating " + f.getName() + ".");
        }
        return new TerminalRet("");
    }
}
