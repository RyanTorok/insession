package searchengine;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {

    private int charAt = -1;
    private String query;
    private char nullChar = (char) 0;
    private Token current;
    private ArrayList<Tag> tags;
    private Token reserve = null;

    public Tokenizer(String query) {
        this.query = query;
        tags = new ArrayList<>();
    }

    public Token peek() {
        return current;
    }

    public Token consume() {
        current = consumeToken();
        while (current.getType() == Token.Type.TAG) {
            tags.add(new Tag(current.getText()));
            current = consumeToken();
        }
        return current;
    }

    private Token consumeToken() {
        if (reserve != null) {
            Token tok = reserve;
            reserve = null;
            return tok;
        }
        next();
        while (Character.toString(get()).matches("\\s+")) {
            next();
        }
        switch (get()) {
            case (char) 0: return new Token(Token.Type.NONE);
            case '\"': {
                StringBuilder phrase = new StringBuilder();
                while (next() != '\"' && get() != nullChar) {
                    if (Character.isLetterOrDigit(get()) || Character.isSpaceChar(get()))
                        phrase.append(get());
                }
                String phraseStr = phrase.toString().toLowerCase().trim();
                Pattern pattern = Pattern.compile("\\s");
                Matcher matcher = pattern.matcher(phraseStr);
                if (!matcher.find()) {
                    Token toReturn = new Token(Token.Type.WORD, phraseStr);
                    reserve = toReturn;
                    return new Token(Token.Type.PLUS);
                }
                return new Token(Token.Type.PHRASE, phraseStr);
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
                StringBuilder word = new StringBuilder(String.valueOf(get()));
                while (Character.isLetterOrDigit(next())) {
                    word.append(get());
                }
                //back up off the first character of the next token
                charAt--;
                String wordStr = word.toString().toLowerCase();
                if (wordStr.equals("and"))
                    return new Token(Token.Type.AND);
                if (wordStr.equals("or"))
                    return new Token(Token.Type.OR);
                return new Token(Token.Type.WORD, word.toString().toLowerCase());
            }
        }
    }

    private char get() {
        if (charAt >= query.length())
            return nullChar;
        return query.charAt(charAt);
    }

    private char next() {
        if (++charAt >= query.length())
            return nullChar;
        return query.charAt(charAt);
    }

    public boolean hasNext() {
        if (reserve != null)
            return false;
        int orig = charAt;
        while (Character.isSpaceChar(next())) {}
        if (get() == nullChar) {
            charAt = orig;
            return false;
        }
        charAt = orig;
        return true;
    }

    ArrayList<Tag> getTags() {
        return tags;
    }
}
