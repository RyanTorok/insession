package searchengine;

public class Token {


    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isPlus() {
        return plus;
    }

    public void setPlus(boolean plus) {
        this.plus = plus;
    }

    public enum Type {
        WORD, PHRASE, AND, OR, NOT, PLUS, LPAREN, RPAREN, NONE, TAG
    }

    public Token(Type type, String text) {
        this.setType(type);
        this.setText(text);
    }

    public Token(Type t) {
        this(t, "");
    }

    private Type type;
    private String text;
    private boolean plus;
}
