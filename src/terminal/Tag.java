package terminal;

import java.util.List;

public class Tag extends Token {

    private Label label;

    public Tag(Label l) {
        super(Token.Type.TAG, l.toString());
        this.label = l;
    }

    public enum Label {
        PARENT, SHORTCUT, ALL, INCLUDE
    }

    public void execute(Object element, List<Token> tokens) {
        assert tokens.get(0).getType() == Token.Type.TAG;

    }

    public static Tag fromString(String tagName) throws TerminalException { //all tags begin with a backslash.
        switch (tagName) {
            case "parent":
                return new Tag(Label.PARENT);
            case "shortcut": return new Tag(Label.SHORTCUT);
            case "include": return new Tag(Label.INCLUDE);
            case "a": return new Tag(Label.ALL);
            default: throw new TerminalException("unrecognized tag name: " + tagName);

        }
    }
}
