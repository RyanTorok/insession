package searchengine;

import static searchengine.Token.Type.*;

public class ParseTree {

    private ParseTree left;
    private ParseTree right;
    private ParseTree parent;
    private String word;
    private Token.Type type;
    private boolean parentDirection;
    private boolean plus = false;
    private boolean negative = false;
    private boolean phrase = false;

    ParseTree(Token.Type type) {
        this(type, null);
    }

    ParseTree(Token.Type type, String word) {
        this.setType(type);
        this.setWord(word);
    }

    static ParseTree fromQuery(String query) {
        Tokenizer tok = new Tokenizer(query);
        ParseTree tree = new ParseTree(null);
        tree.parse(tok, false, false);
        return tree.getRoot();
    }

    private ParseTree getRoot() {
        ParseTree tree = this;
        while (tree.parent != null) {
            tree = tree.parent;
        }
        return tree;
    }

    private void parse(Tokenizer tok, boolean negate, boolean plus) {
        this.negative = negate;
        this.plus = plus;
        Token me = tok.consume();
        switch (me.getType()) {
            case NONE:
                break;
            case LPAREN: {
                if (left == null) {
                    //left child is empty, place sub-query in left child
                    left = new ParseTree(null) {
                        {
                            parse(tok, negate, false);
                        }
                    };
                } else {
                    right = new ParseTree(null);
                    right.setLeft(new ParseTree(null) {
                        {
                            parse(tok, negate, false);
                        }
                    });
                }
                break;
            }
            case RPAREN:
                break;
            case AND: {
                if (type == null) {
                    type = AND;
                    right = new ParseTree(null) {
                        {
                            parse(tok, negate, false);
                        }
                    };
                } else {
                    ParseTree newTree = new ParseTree(AND);
                    setRight(newTree);
                    rotateLeft();
                    newTree.right = new ParseTree(null) {
                        {
                            parse(tok, negate, false);
                        }
                    };
                    if (right.getType() == OR) {
                        //ensures OR nodes have lower precedence than AND nodes by being farther up in the tree
                        //ORs nested in () are immune because they are only ever placed in the left child
                        rotateLeft();
                    }
                }
                break;
            }
            case OR: {
                if (type == null) {
                    type = OR;
                    right = new ParseTree(null) {
                        {
                            parse(tok, negate, false);
                        }
                    };
                } else {
                    ParseTree newTree = new ParseTree(OR);
                    setRight(newTree);
                    rotateLeft();
                    newTree.right = new ParseTree(null) {
                        {
                            parse(tok, negate, false);
                        }
                    };

                }
                break;
            }
            case NOT: parse(tok, !negate, plus); break;
            case PLUS: parse(tok, negate, true); break;
            case PHRASE:
            case WORD: {
                if (left == null) {
                    left = new ParseTree(WORD, me.getText());
                    left.plus = plus;
                    left.phrase = me.getType() == PHRASE;
                    parse(tok, negate, false);
                } else {
                    if (type == null) {
                        //consecutive words, need implicit AND
                        this.type = AND;
                        ParseTree newTree = new ParseTree(null) {
                            {
                                parse(tok, negate, false);
                            }
                        };
                        setRight(newTree);
                        rotateLeft();
                    }
                }
                break;
            }
        }
    }

    private void rotateLeft() {
        assert right != null;
        ParseTree child = right;
        if (getParent() != null) {
            if (!parentDirection)
                getParent().setLeft(child);
            else getParent().setRight(child);
        }
        child.parentDirection = this.parentDirection;
        this.parentDirection = false;
        setRight(child.getLeft());
        child.setLeft(this);
        child.setParent(getParent());
        this.setParent(child);
    }

    public ParseTree getLeft() {
        return left;
    }

    public void setLeft(ParseTree left) {
        this.left = left;
        right.setParent(this);
        right.parentDirection = false;
    }

    public ParseTree getRight() {
        return right;
    }

    public void setRight(ParseTree right) {
        this.right = right;
        right.setParent(this);
        right.parentDirection = true;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Token.Type getType() {
        return type;
    }

    public void setType(Token.Type type) {
        this.type = type;
    }

    public ParseTree getParent() {
        return parent;
    }

    public void setParent(ParseTree parent) {
        this.parent = parent;
    }


    public boolean isNegative() {
        return negative;
    }

    public void setNegative(boolean negative) {
        this.negative = negative;
    }

    public boolean isPhrase() {
        return phrase;
    }

    public void setPhrase(boolean phrase) {
        this.phrase = phrase;
    }

    public boolean isPlus() {
        return plus;
    }

    public void setPlus(boolean plus) {
        this.plus = plus;
    }
}