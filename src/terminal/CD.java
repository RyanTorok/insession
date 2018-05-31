package terminal;

import java.io.File;
import java.util.ArrayList;

public class CD extends Command {
    @Override
    protected TerminalRet execute(ArrayList<Token> tokens) throws TerminalException{
        assertTokenCount(2, tokens, "destination directory");
        String dest = tokens.get(1).getTokenLabel();
        File current = Address.parse(dest, true, false, true, false, false);
        System.setProperty("user.dir", current.getPath());
        return new TerminalRet("", false, false);
    }
}
