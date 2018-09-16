package gui;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class KeyMap implements Serializable {

    static final long serialVersionUID = 400L;

    public static final int ALL_STATES = 0;
    public static final KeyCombo ALL = null;
    public static final Boolean BOTH = null;

    private HashMap<Integer, HashMap<KeyCombo, Consumer<Node>>> table;
    private HashMap<Integer, Consumer<Node>> defaultTable;
    private boolean consume = false;
    private boolean locked = false;

    public KeyMap() {
        table = new HashMap<>();
        defaultTable = new HashMap<>();
    }

    // returns true if the association succeeds. This function is just a wrapper for the real association code.
    // most of the code here just deals with the strange case of resetting the first map if homeScreen == BOTH and the first association succeeds and the second fails.
    public boolean associate(int state, Boolean homeScreen, KeyCombo shortcut, Consumer<Node> function) {
        if (homeScreen == BOTH) {
            HashMap<KeyCombo, Consumer<Node>> wrapper = table.computeIfAbsent(state, k -> new HashMap<>());
            Consumer<Node> prev = wrapper.get(shortcut);
            KeyCombo r1 = associate(state, shortcut, function);
            KeyCombo r2 = associate(-1 * state, shortcut, function);
            boolean b1 = r1 != null, b2 = r2 != null;
            if (shortcut == ALL)
                return b1 && b2;
            if (b1 && !b2) {
                wrapper.remove(r1);
                wrapper.put(shortcut, prev);
            }
            return b1 && b2;


        }
        return associate(homeScreen ? state : -1 * state, shortcut, function) != null;
    }

    public String associate(int state, Boolean homeScreen, String shortcut, Consumer<Node> function) {
        try {
            return String.valueOf(associate(state, homeScreen, KeyCombo.parse(shortcut), function));
        } catch (IllegalKeyComboException e) {
            return e.getMessage();
        }
    }

    // returns argument shortcut if the association is successful with no conflict.
    // returns displacement key of old element if there was a conflict, or null if the displacement is not successful (i.e. all shortcuts are full for given state).
    // state: represents state value of Root.getPortal(), positive for homeScreen = true, negative for homeScreen = false (i.e. TaskViews open or tiled)
    private KeyCombo associate(int state, KeyCombo shortcut, Consumer<Node> function) {
        if (shortcut == ALL)
            return associateDefault(state, function);
        HashMap<KeyCombo, Consumer<Node>> entry = table.computeIfAbsent(state, k -> new HashMap<>());
        Consumer<Node> existing = entry.get(shortcut);
        entry.put(shortcut, function);
        if (existing != null) {
            KeyCombo next = nextAvailable(state, shortcut);
            if (next != null) {
                entry.put(next, existing);
                return next;
            } else {
                entry.put(shortcut, existing);
                return null;
            }
        }
        return shortcut;
    }

    private KeyCombo associateDefault(int state, Consumer<Node> function) {
        if (defaultTable.get(state) != null)
            return null;
        defaultTable.put(state, function);
        return new KeyCombo(null, KeyCode.A);
    }

    private KeyCombo nextAvailable(int state, KeyCombo old) {
        KeyCombo current = new KeyCombo(new HashSet<>(), old.key);
        while (true) {
            current.functionKeys.clear();
            current.functionKeys.add(FunctionKey.CONTROL);
            if (testKey(state, current)) //CTRL + <char>
                return current;
            current.functionKeys.add(FunctionKey.SHIFT);
            if (testKey(state, current)) //CTRL + SHIFT + <char>
                return current;
            current.functionKeys.remove(FunctionKey.SHIFT);
            current.functionKeys.add(FunctionKey.ALT);
            if (testKey(state, current)) //CTRL + ALT + <char>
                return current;
            current.functionKeys.remove(FunctionKey.CONTROL);
            current.functionKeys.add(FunctionKey.SHIFT);
            if (testKey(state, current)) //ALT + SHIFT + <char>
                return current;
            current.functionKeys.add(FunctionKey.CONTROL);
            if (testKey(state, current)) //CTRL + ALT + SHIFT + <char>
                return current;
            current = next(current);
            if (current.key.equals(old.key)) {
                //we cycled all the way around and every possible shortcut is full, just give up.
                return null;
            }
        }
    }

    private boolean testKey(int state, KeyCombo current) {
        return !table.computeIfAbsent(state, k -> new HashMap<>()).containsKey(current);
    }

    private KeyCombo next(KeyCombo current) {
        KeyCode old = current.key;
        boolean reached = false;
        for (KeyCode code : KeyCode.values()) {
            if (code.equals(old)) {
                reached = true;
                continue;
            }
            if (!reached)
                continue;
            if (Character.isLetterOrDigit(code.getChar().charAt(0)) || Character.getType(code.getChar().charAt(0)) == Character.MATH_SYMBOL)
                return new KeyCombo(new HashSet<>(), code);
        }
        for (KeyCode code : KeyCode.values()) {
            if (Character.isLetterOrDigit(code.getChar().charAt(0)) || Character.getType(code.getChar().charAt(0)) == Character.MATH_SYMBOL)
                return new KeyCombo(new HashSet<>(), code);
        }
        return null; //should never get here
    }

    boolean fireEvent(KeyEvent event, int state, boolean negative) {
       return fireEvent(event, null, state, negative);
    }

    private boolean fireEvent(KeyEvent event, Node target, int state, boolean homescreen) {
        if (locked)
            return false;
        if (state == 0 || state < -10 || state > 10)
            throw new IllegalArgumentException("Illegal state parameter for event");
        KeyCombo gen = new KeyCombo(new HashSet<>(), event.getCode());
        if (event.isControlDown())
            gen.functionKeys.add(FunctionKey.CONTROL);
        if (event.isAltDown())
            gen.functionKeys.add(FunctionKey.ALT);
        if (event.isShiftDown())
            gen.functionKeys.add(FunctionKey.SHIFT);

        if (!executeOne(homescreen ? state : -1 * state, target, gen)) {
            executeOne(0, target, gen);
        }
        boolean toReturn = consume;
        consume = false;
        return toReturn;
    }

    private boolean executeOne(int state, Node target, KeyCombo gen) {
        HashMap<KeyCombo, Consumer<Node>> entry = table.get(state);
        if (entry != null) {
            Consumer<Node> fun = entry.get(gen);
            if (fun != null) {
                fun.accept(target);
                return true;
            }
        }
        Consumer<Node> defaultFun = defaultTable.get(state);
        if (defaultFun != null) {
            defaultFun.accept(target);
            return true;
        }
        return false;
    }

    public void consume() {
        consume = true;
    }

    public void lock() {
        locked = true;
    }

    public void unlock() {
        locked = false;
    }

    enum FunctionKey {
        CONTROL, ALT, SHIFT
    }

    private static class KeyCombo {
        Set<FunctionKey> functionKeys;
        KeyCode key;

        KeyCombo(Set<FunctionKey> functionKeys, char key) {
            this.functionKeys = functionKeys;
            this.key = key == ' ' ? KeyCode.SPACE : KeyCode.getKeyCode(Character.toString(key).toUpperCase());
        }

        KeyCombo(Set<FunctionKey> keys, KeyCode code) {
            functionKeys = keys;
            key = code;
        }

        public static KeyCombo parse(String combo) throws IllegalKeyComboException {
            HashSet<FunctionKey> functionKeys = new HashSet<>();
            KeyCode invisibleOverride = null;
            char argChar = (char) 0;
            String[] keys = combo.split("\\+");
            for (String key : keys) {
                key = key.toLowerCase().trim();
                switch (key) {
                    case "control":
                    case "ctrl":
                    case "cmd":
                    case "command":
                        functionKeys.add(FunctionKey.CONTROL);
                        break;
                    case "alt":
                    case "option":
                        functionKeys.add(FunctionKey.ALT);
                        break;
                    case "shift":
                        functionKeys.add(FunctionKey.SHIFT);
                        break;
                    case "space":
                        argChar = ' ';
                        break;
                    case "escape":
                        invisibleOverride = KeyCode.ESCAPE;
                        break;
                    case "backspace":
                        invisibleOverride = KeyCode.BACK_SPACE;
                        break;
                    case "enter":
                    case "return":
                        invisibleOverride = KeyCode.ENTER;
                        break;
                    case "home":
                        invisibleOverride = KeyCode.HOME;
                        break;
                    case "end":
                        invisibleOverride = KeyCode.END;
                        break;
                    case "page up":
                    case "pgup":
                    case "pageup":
                        invisibleOverride = KeyCode.PAGE_UP;
                        break;
                    case "page down":
                    case "pagedown":
                    case "pgdown":
                    case "page dn":
                    case "pagedn":
                    case "pgdn":
                        invisibleOverride = KeyCode.PAGE_DOWN;
                        break;
                    case "insert":
                        invisibleOverride = KeyCode.INSERT;
                        break;
                    case "del":
                    case "delete":
                        invisibleOverride = KeyCode.DELETE;
                        break;
                    case "prtsc":
                    case "prt sc":
                    case "printscreen":
                    case "print screen":
                        invisibleOverride = KeyCode.PRINTSCREEN;
                        break;
                    case "pause":
                        invisibleOverride = KeyCode.PAUSE;
                        break;
                    case "f1":
                        invisibleOverride = KeyCode.F1;
                        break;
                    case "f2":
                        invisibleOverride = KeyCode.F2;
                        break;
                    case "f3":
                        invisibleOverride = KeyCode.F3;
                        break;
                    case "f4":
                        invisibleOverride = KeyCode.F4;
                        break;
                    case "f5":
                        invisibleOverride = KeyCode.F5;
                        break;
                    case "f6":
                        invisibleOverride = KeyCode.F6;
                        break;
                    case "f7":
                        invisibleOverride = KeyCode.F7;
                        break;
                    case "f8":
                        invisibleOverride = KeyCode.F8;
                        break;
                    case "f9":
                        invisibleOverride = KeyCode.F9;
                        break;
                    case "f10":
                        invisibleOverride = KeyCode.F10;
                        break;
                    case "f11":
                        invisibleOverride = KeyCode.F11;
                        break;
                    case "f12":
                        invisibleOverride = KeyCode.F12;
                        break;
                    case "tab":
                        invisibleOverride = KeyCode.TAB;
                        break;
                    case "caps":
                    case "capslock":
                    case "caps lock":
                        invisibleOverride = KeyCode.CAPS;
                        break;
                    case "up":
                    case "uparrow":
                    case "up arrow":
                        invisibleOverride = KeyCode.UP;
                        break;
                    case "left":
                    case "leftarrow":
                    case "left arrow":
                        invisibleOverride = KeyCode.LEFT;
                        break;
                    case "right":
                    case "rightarrow":
                    case "right arrow":
                        invisibleOverride = KeyCode.RIGHT;
                        break;
                    case "down":
                    case "downarrow":
                    case "down arrow":
                    case "dn":
                    case "dnarrow":
                    case "dn arrow":
                        invisibleOverride = KeyCode.DOWN;
                        break;
                    case "-":
                    case "minus":
                    case "hyphen":
                    case "dash":
                        invisibleOverride = KeyCode.MINUS;
                        break;
                    case "/":
                    case "fwslash":
                    case "forwardslash":
                    case "forward slash":
                    case "fwdslash":
                    case "fwd slash":
                    case "slash":
                        invisibleOverride = KeyCode.SLASH;
                        break;
                    case ",":
                    case "comma":
                        invisibleOverride = KeyCode.COMMA;
                        break;
                    case ".":
                    case "period":
                    case "fullstop":
                    case "full stop":
                    case "decimal":
                    case "point":
                    case "dot":
                        invisibleOverride = KeyCode.PERIOD;
                        break;
                    case "`":
                    case "back quote":
                    case "backquote":
                        invisibleOverride = KeyCode.BACK_QUOTE;
                        break;
                    case "'":
                    case "quote":
                    case "apostrophe":
                        invisibleOverride = KeyCode.QUOTE;
                        break;
                    case ";":
                    case "semi":
                    case "semicolon":
                        invisibleOverride = KeyCode.SEMICOLON;
                    case "[":
                    case "lbracket":
                    case "left bracket":
                    case "leftbracket":
                        invisibleOverride = KeyCode.OPEN_BRACKET;
                        break;
                    case "]":
                    case "rbracket":
                    case "right bracket":
                    case "rightbracket":
                        invisibleOverride = KeyCode.CLOSE_BRACKET;
                        break;
                    case "1":
                    case "one":
                        invisibleOverride = KeyCode.DIGIT1;
                        break;
                    case "2":
                    case "two":
                        invisibleOverride = KeyCode.DIGIT2;
                        break;
                    case "3":
                    case "three":
                        invisibleOverride = KeyCode.DIGIT3;
                        break;
                    case "4":
                    case "four":
                        invisibleOverride = KeyCode.DIGIT4;
                        break;
                    case "5":
                    case "five":
                        invisibleOverride = KeyCode.DIGIT5;
                        break;
                    case "6":
                    case "six":
                        invisibleOverride = KeyCode.DIGIT6;
                        break;
                    case "7":
                    case "seven":
                        invisibleOverride = KeyCode.DIGIT7;
                        break;
                    case "8":
                    case "eight":
                        invisibleOverride = KeyCode.DIGIT8;
                        break;
                    case "9":
                    case "nine":
                        invisibleOverride = KeyCode.DIGIT9;
                        break;
                    case "0":
                    case "zero":
                    case "naught":
                    case "nought":
                        invisibleOverride = KeyCode.DIGIT0;
                        break;
                    default:
                        if (key.length() > 1 || argChar != 0)
                            throw new IllegalKeyComboException("Illegal Key Combination Expression for Keyboard Shortcut: " + key);
                        else argChar = key.charAt(0);
                }
            }
            if (invisibleOverride != null)
                return new KeyCombo(functionKeys, invisibleOverride);
            if (argChar == 0)
                throw new IllegalArgumentException("No non-function key provided for keyboard shortcut." + combo);
            else return new KeyCombo(functionKeys, argChar);

        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof KeyCombo))
                return false;
            return functionKeys.equals(((KeyCombo) obj).functionKeys) && key.equals(((KeyCombo) obj).key);
        }

        @Override
        public int hashCode() {
            int c = functionKeys.contains(FunctionKey.CONTROL) ? 1 : 0;
            int a = functionKeys.contains(FunctionKey.ALT) ? 1 : 0;
            int s = functionKeys.contains(FunctionKey.SHIFT) ? 1 : 0;
            return (key.getCode() * 8) + ((4 * c) + (2 * a) + s);
        }
    }
}
