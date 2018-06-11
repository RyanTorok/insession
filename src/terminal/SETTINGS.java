package terminal;

import gui.AcctSettings;
import main.Root;

import java.util.ArrayList;

public class SETTINGS extends Command {
    @Override
    protected TerminalRet execute(ArrayList<Token> tokens) throws TerminalException {
        assertTokenCount(1, tokens, "no arguments");
        if (Root.getActiveUser() != null && Root.getActiveUser().getUsername() != null) {
            new AcctSettings().show();
        } else {
            throw new TerminalException("settings - cannot open account settings when browsing as a guest.");
        }
        return new TerminalRet("", true, false);
    }
}
