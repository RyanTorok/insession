package terminal;

import gui.AcctSettings;
import main.User;

import java.util.ArrayList;

public class SETTINGS extends Command {
    @Override
    protected TerminalRet execute(ArrayList<Token> tokens) throws TerminalException {
        assertTokenCount(1, tokens, "no arguments");
        if (User.active() != null && User.active().getUsername() != null) {
            new AcctSettings().show();
        } else {
            throw new TerminalException("settings - cannot open account settings when browsing as a guest.");
        }
        return new TerminalRet("");
    }
}
