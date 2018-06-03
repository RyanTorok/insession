package terminal;

import db.LoginException;
import db.SQLMaster;
import gui.NewUserWindow;
import main.Root;
import main.User;

import java.util.ArrayList;

public class SWITCH extends Command{

    @Override
    protected TerminalRet execute(ArrayList<Token> tokens) throws TerminalException {
        if (tokens.size() == 1) {
            Root.getActiveUser().write();
            Root.getPortal().newUser();
            return new TerminalRet("", true, true);
        }
        if (tokens.get(1).getType() == Token.Type.TAG) {
            if (tokens.get(1).getTokenLabel().equalsIgnoreCase("login")) {
                assertTokenCount(4, tokens, "username and password of destination account after \\l");
                try {
                    User newUser = SQLMaster.login(tokens.get(2).getTokenLabel(), tokens.get(3).getTokenLabel());
                    Root.setActiveUser(newUser);
                    Root.getPortal().clearStage();
                    Root.getPortal().switchToMain();
                } catch (LoginException e) {
                    boolean loginConnectionErr = Boolean.valueOf(e.getMessage());
                    throw new TerminalException(loginConnectionErr ? "switch - a connection error occurred." : "switch - the provided username and password combination is not recognized.");
                }
            } else {
                throw new TerminalException("switch - unexpected tag '"+ ((Tag) tokens.get(1)).getName() + "'.");
            }
        }
        assertTokenCount(2, tokens, "username of destination account");
        if (tokens.get(1).getTokenLabel().equals(Root.getActiveUser().getUsername())) {
            throw new TerminalException("switch - user '" + tokens.get(1).getTokenLabel() + "' is already signed in.");
        }
        User newUser = User.read(tokens.get(1).getTokenLabel());
        if (newUser == null) {
            throw new TerminalException("switch - user '" + tokens.get(1).getTokenLabel() + "' was not found.");
        }
        Root.getPortal().resetMain();
        return new TerminalRet("", false, false);
    }
}