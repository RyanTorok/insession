package searchengine;

import java.util.ArrayList;

public class Tokenizer {

    private int charAt = -1;
    private String query;
    private char nullChar = (char) 0;
    private Token current;
    private ArrayList<Tag> tags;

    public Tokenizer(String query) {
        this.query = query;
    }

    public Token peek() {
        return current;
    }

    public Token consume() {
        current = consumeToken();
        while (current.getType() == Token.Type.TAG)
        while (current.getType() == Token.Type.TAG) {
            tags.add(new Tag(current.getText()));
            current = consumeToken();
        }
        return current;
    }

    private Token consumeToken() {
        switch (next()) {
            case (char) 0: return new Token(Token.Type.NONE);
            case '\"': {
                StringBuilder phrase = new StringBuilder();
                while (next() != '\"' && get() != nullChar) {
                    phrase.append(get());
                }
                return new Token(Token.Type.PHRASE, phrase.toString());
            }
            case '-': return new Token(Token.Type.NOT);
            case '+': return new Token(Token.Type.PLUS);
            case '&': return new Token(Token.Type.AND);
            case '|': return new Token(Token.Type.OR);
            case '(': return new Token(Token.Type.LPAREN);
            case ')': return new Token(Token.Type.RPAREN);
            case '\\': {
                StringBuilder tag = new StringBuilder();
                while (next() != '\"' && get() != nullChar) {
                    tag.append(get());
                }
                //back up off the first character of the next token
                charAt--;
                return new Token(Token.Type.TAG, tag.toString());
            }
            default: {
                //search word
                StringBuilder word = new StringBuilder();
                while (Character.isLetterOrDigit(next())) {
                    word.append(get());
                }
                //back up off the first character of the next token
                charAt--;
                return new Token(Token.Type.WORD, word.toString());
            }
        }
    }

    private char get() {
        if (charAt >= query.length())
            return nullChar;
        return query.charAt(charAt);
    }

    private char next() {
        if (charAt >= query.length())
            return nullChar;
        return query.charAt(++charAt);
    }
}
