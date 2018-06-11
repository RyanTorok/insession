package terminal;

import java.util.ArrayList;

public abstract class Command {
    String name;
    String alias;
static int test = 0;
    /*
        Performs any action specified by the command, then returns any text to be printed.
        If there is no output, returns "Success" to verify the command succeeded.
     */
    protected abstract TerminalRet execute(ArrayList<Token> tokens) throws TerminalException;

    static TerminalRet command(String input) throws TerminalException {
        input = input.trim();
        if (input.length() == 0) {
            return new TerminalRet("", false, false);
        }
        Tokenizer tok = new Tokenizer();
        Command c;
        try {
            tok.loadCommand(input);
            c = tok.get();
        } catch (TerminalException e) {
            return new TerminalRet("Terminal: " + e.getMessage(), false, false);
        }
        return c.execute(tok.getTokens());
    }

    public enum CommandType {
        MKDIR, LS, PWD, NEW, CD, TAKE, SYNC, CREATE, TOUCH, EDIT, DELETE,
        RM, RUN, CAT, SEE, LOOKAT, SEARCH, SWITCH, SUBMIT, TURNIN, CLEAR, EXIT, HIDE, OPEN, SETTINGS, DEFINE
    }


    public void assertTokenCount(int expected, ArrayList<Token> tokens, String what) throws TerminalException {
        if (tokens.size() != expected) {
            throw new TerminalException("wrong number of arguments for command '" + this.getClass().getSimpleName().toLowerCase() + "', expected " + what + ".");
        }
    }

    public void assertTokenCount(int expected, ArrayList<Token> tokens) throws TerminalException {
        assertTokenCount(expected, tokens, expected + " arguments");
    }
}
