package terminal;

import java.util.List;

public class Tag extends Token {

    private Label label;
    private String name;

    public Tag(Label l, String tagName) {
        super(Token.Type.TAG, l.toString());
        this.setLabel(l);
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public enum Label {
        PARENT, SHORTCUT, ALL, LOGIN, INCLUDE
    }

    public void execute(Object element, List<Token> tokens) {
        assert tokens.get(0).getType() == Token.Type.TAG;

    }

    public static Tag fromString(String tagName) throws TerminalException { //all tags begin with a backslash.
        switch (tagName) {
            case "parent":
                return new Tag(Label.PARENT, tagName);
            case "shortcut": return new Tag(Label.SHORTCUT, tagName);
            case "include": return new Tag(Label.INCLUDE, tagName);
            case "a": return new Tag(Label.ALL, tagName);
            case "l": return new Tag(Label.LOGIN, tagName);
            default: throw new TerminalException("unrecognized tag name: " + tagName);

        }
    }
}
