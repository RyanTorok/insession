package terminal;

import java.util.ArrayList;

public class Tokenizer {

    private ArrayList<Token> tokens;

    private Token parseToken(String s, boolean inst) throws TerminalException {
        if (s.charAt(0) == '\\')
            return Tag.fromString(s.substring(1));
        if (inst) {
            switch (s.toLowerCase()) {
                case "ls": return new Token(Command.CommandType.LS);
                case "cd": return new Token(Command.CommandType.CD);
                case "pwd": return new Token(Command.CommandType.PWD);
                case "mkdir": return new Token(Command.CommandType.MKDIR);
                case "new": return new Token(Command.CommandType.NEW);
                case "take": return new Token(Command.CommandType.TAKE);
                case "sync": return new Token(Command.CommandType.SYNC);
                case "create": return new Token(Command.CommandType.CREATE);
                case "touch": return new Token(Command.CommandType.TOUCH);
                case "edit": return new Token(Command.CommandType.EDIT);
                case "delete": return new Token(Command.CommandType.DELETE);
                case "rm": return new Token(Command.CommandType.RM);
                case "run": return new Token(Command.CommandType.RUN);
                case "cat": return new Token(Command.CommandType.CAT);
                case "see": return new Token(Command.CommandType.SEE);
                case "search": return new Token(Command.CommandType.SEARCH);
                case "switch": return new Token(Command.CommandType.SWITCH);
                case "submit": return new Token(Command.CommandType.SUBMIT);
                case "turnin": return new Token(Command.CommandType.TURNIN);
                case "define": return new Token(Command.CommandType.DEFINE);
                case "clear": return new Token(Command.CommandType.CLEAR);
                case "exit": return new Token(Command.CommandType.EXIT);
                case "hide": return new Token(Command.CommandType.HIDE);
                case "quit": return new Token(Command.CommandType.EXIT);
                case "open": return new Token(Command.CommandType.OPEN);
                case "settings": case "options": case "prefrences": return  new Token(Command.CommandType.SETTINGS);
                default: throw new TerminalException(s + " - Command not found");
            }
        }
        else {
            return new Token(Token.Type.VARIABLE, s);
        }

    }

    public void loadCommand(String input) throws TerminalException {
        setTokens(new ArrayList<>());
        String[] split = input.trim().split("\\s+");
        int index = 0;
        for (String s : split) {
            getTokens().add(parseToken(s, index++ == 0));
        }
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }

    public void setTokens(ArrayList<Token> tokens) {
        this.tokens = tokens;
    }

    public Command get() throws TerminalException {
        if (tokens.get(0).getType() != Token.Type.COMMAND)
            throw new TerminalException("non-command token in index 0");
        switch (tokens.get(0).getCtype()) {
            case LS: return new LS();
            case PWD: return new PWD();
            case EXIT: return new QUIT();
            case HIDE: return new HIDE();
            case CLEAR: return new CLEAR();
            case CD: return new CD();
            case MKDIR: return new MKDIR();
            case RM: case DELETE: return new RM();
            case TOUCH: return new TOUCH();
            case CAT: return new CAT();
            case LOOKAT: return new CAT();
            case SEE: return new CAT();
            case OPEN: return new OPEN();
            case SWITCH: return new SWITCH();
            case EDIT: return new EDIT();
            case SETTINGS: return new SETTINGS();
            case SEARCH: return new SEARCH();
        }
        return null;
    }
}
