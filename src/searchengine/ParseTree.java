package searchengine;

import classes.Test;

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

    static ParseTree fromQuery(Tokenizer tok) {
        ParseTree tree = new ParseTree(null);
        tree.parseWrapper(tok);
        return tree.getRoot();
    }

    private ParseTree getRoot() {
        ParseTree tree = this;
        while (tree.parent != null) {
            tree = tree.parent;
        }
        return tree;
    }

    //percolates down a negative branch using DeMorgan's Law
    private void deepNegate() {
        if (type == AND) type = OR;
        else if (type == OR) type = AND;
        else if (type == WORD || type == PHRASE)
            negative = !negative;
        if (left != null)
            left.deepNegate();
        if (right != null)
            right.deepNegate();
    }

    private void parseWrapper(Tokenizer tok) {
        parse(tok, false, false);
        if (tok.hasNext()) {
            ParseTree superTree = new ParseTree(null);
            superTree.setLeft(this);
            superTree.parseWrapper(tok);
        }
    }

    private void parse(Tokenizer tok, boolean negate, boolean plus) {
        boolean rParen = false;
        Token me = tok.consume();
        switch (me.getType()) {
            case NONE: {
                //end of query, rotate ending parenthesised query right to prevent inner null node
                if (type == null && left != null && left.getType() != null) {
                    //consume child node into empty parent
                    consumeChild(left);
                }
                break;
            }
            case LPAREN: {
                if (type == null) {
                    if (left == null) {
                        //place parenthesized query in left child
                        ParseTree subTree = new ParseTree(null);
                        subTree.parse(tok, false, false);
                        setLeft(subTree.getRoot());
                        if (negate)
                            left.deepNegate();
                        parse(tok, false, false);
                    } else {
                        //previous query in left child but no operator: need implicit AND
                        type = AND;
                        ParseTree newRight = new ParseTree(null);
                        ParseTree newRightLeft = new ParseTree(null);
                        newRightLeft.parse(tok, false, false);
                        newRightLeft = newRightLeft.getRoot();
                        if (negate)
                            newRightLeft.deepNegate();
                        newRight.setLeft(newRightLeft);
                        setRight(newRight);
                        right.parse(tok, false, false);
                        if (right.type == null)
                            right.consumeChild(right.left);
                    }
                } else {
                    if (type == AND || type == OR) {
                        //same as above, but without implicit AND
                        ParseTree newRight = new ParseTree(null);
                        ParseTree newRightLeft = new ParseTree(null);
                        newRightLeft.parse(tok, false, false);
                        newRightLeft = newRightLeft.getRoot();
                        if (negate)
                            newRightLeft.deepNegate();
                        newRight.setLeft(newRightLeft);
                        setRight(newRight);
                        right.parse(tok, false, false);
                        if (right.type == null)
                            right.consumeChild(right.left);
                    } else {
                        //word or phrase query in root
                        ParseTree andNode = new ParseTree(AND);
                        setRight(andNode);
                        rotateLeft();
                        assert andNode.left == this;
                        ParseTree newRight = new ParseTree(null);
                        ParseTree newRightLeft = new ParseTree(null);
                        newRightLeft.parse(tok, false, false);
                        newRightLeft = newRightLeft.getRoot();
                        if (negate)
                            newRightLeft.deepNegate();
                        newRight.setLeft(newRightLeft);
                        andNode.setRight(newRight);
                        andNode.right.parse(tok, false, false);
                        if (andNode.right != null && andNode.right.type == null)
                            andNode.right.consumeChild(andNode.right.left);

                    }
                }
                break;
            }
            case RPAREN: {
                if (type == null && left != null && left.getType() != null) //handles empty nodes created by nested parentheses
                    consumeChild(left);
                break;
            }
            case AND: {
                if (type == null) {
                    if (left == null)
                        parse(tok, false, false);
                    else {
                        type = AND;
                        ParseTree newTree = new ParseTree(null);
                        newTree.parse(tok, false, false);
                        setRight(newTree.getRoot());
                        if (right.getType() == OR) rotateLeft();
                    }
                } else {
                    ParseTree newTree = new ParseTree(AND);
                    setRight(newTree);
                    rotateLeft();
                    ParseTree newTree1 = new ParseTree(null);
                    newTree1.parse(tok, false, false);
                    newTree.right = newTree1.getRoot();
                    if (newTree.right.getType() == OR) {
                        //ensures OR nodes have lower precedence than AND nodes by being farther up in the tree
                        //ORs nested in () are immune because they are only ever placed in the left child
                        newTree.rotateLeft();
                    }
                }
                break;
            }
            case OR: {
                if (type == null) {
                    if (left == null) {
                        parse(tok, false, false);
                    } else {
                        type = OR;
                        ParseTree newTree = new ParseTree(null);
                        newTree.parse(tok, false, false);
                        right = newTree.getRoot();
                    }
                } else {
                    ParseTree newTree = new ParseTree(OR);
                    setRight(newTree);
                    rotateLeft();
                    ParseTree newTree1 = new ParseTree(null);
                    newTree1.parse(tok, false, false);
                    newTree.setRight(newTree1.getRoot());
                }
                break;
            }
            case NOT: parse(tok, !negate, plus); break;
            case PLUS: parse(tok, negate, true); break;
            case PHRASE:
            case WORD: {
                if (type == null) {
                    if (left == null || left.type == null) {
                        type = me.getType();
                        word = me.getText();
                        this.negative = negate;
                        this.plus = plus;
                        parse(tok, false, false);
                    } else {
                        this.type = AND;
                        ParseTree newRight = new ParseTree(me.getType(), me.getText());
                        newRight.setNegative(negate);
                        newRight.setPlus(plus);
                        newRight.parse(tok, false, false);
                        setRight(newRight.getRoot());
                        if (right.getType() == OR)
                            rotateLeft();
                    }
                } else {
                    if (type == AND || type == OR) {
                        ParseTree newRight = new ParseTree(me.getType(), me.getText());
                        newRight.setNegative(negate);
                        newRight.setPlus(plus);
                        newRight.parse(tok, false, false);
                        setRight(newRight.getRoot());
                        if (type == AND && right.getType() == OR)
                            rotateLeft();
                    } else {
                        ParseTree and = new ParseTree(AND);
                        setRight(and);
                        rotateLeft();
                        assert and.left == this;
                        ParseTree newRight = new ParseTree(me.getType(), me.getText());
                        newRight.setNegative(negate);
                        newRight.setPlus(plus);
                        newRight.parse(tok, false, false);
                        and.setRight(newRight.getRoot());
                        if (and.getRight().getType() == OR)
                            and.rotateLeft();
                    }
                }
                break;
            }
        }
    }

    private void consumeChild(ParseTree tree) {
        this.setType(tree.getType());
        this.setLeft(tree.getLeft());
        this.setRight(tree.getRight());
        this.setNegative(tree.isNegative());
        this.setPlus(tree.isPlus());
        this.setWord(tree.getWord());
        this.setPhrase(tree.isPhrase());
    }

    private void rotateLeft() {
        assert right != null;
        ParseTree child = right;
        if (getParent() != null) {
            if (!parentDirection)
                getParent().plainSetLeft(child);
            else getParent().plainSetRight(child);
        }
        child.parentDirection = this.parentDirection;
        this.parentDirection = false;
        plainSetRight(child.getLeft());
        child.plainSetLeft(this);
        child.setParent(getParent());
        this.setParent(child);
    }

    public ParseTree getLeft() {
        return left;
    }

    private void plainSetLeft(ParseTree tree) {
        this.left = tree;
    }

    private void plainSetRight(ParseTree tree) {
        this.right = tree;
    }

    public void setLeft(ParseTree left) {
        this.left = left;
        if (left != null) {
            left.setParent(this);
            left.parentDirection = false;
        }
    }

    public ParseTree getRight() {
        return right;
    }

    public void setRight(ParseTree right) {
        if (right != null)
        this.right = right;
        if (right != null) {
            right.setParent(this);
            right.parentDirection = true;
        }
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