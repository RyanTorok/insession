package terminal;

public class Token {

    private String tokenLabel;
    private Type type;
    private Command.CommandType ctype;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Command.CommandType getCtype() {
        return ctype;
    }

    public enum Type {
        COMMAND, ADDRESS, VARIABLE, VALUE, IDENTIFIER, TAG, LPAREN, RPAREN, LBRACE, RBRACE, LBRACKET, RBRACKET, EQUALS, LANGLE, RANGLE, COMMA
    }



    public Token(Type t, String label) {
        type = t;
        ctype = Command.CommandType.LS; //default, in case t == COMMAND
        tokenLabel = label;
    }

    public Token(Command.CommandType ctype) {
        type = Type.COMMAND;
        this.ctype = ctype;
    }

    public String getTokenLabel() {
        return tokenLabel;
    }

    @Override
    public String toString() {
        return (tokenLabel == null) ? type.toString().toLowerCase() : tokenLabel;
    }
}