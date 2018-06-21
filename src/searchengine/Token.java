package searchengine;

public class Token {

    public enum Type {
        WORD, PHRASE, AND, OR, NOT, PLUS, LPAREN, RPAREN, TAG
    }

    public Token(Type type, String text) {
        this.type = type;
        this.text = text;
    }

    public Token(Type t) {
        this(t, "");
    }

    private Type type;
    private String text;
}
