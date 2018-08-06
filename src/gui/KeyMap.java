package gui;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class KeyMap {

    public static final int ALL_STATES = 0;

    private HashMap<Integer, HashMap<KeyCombo, Consumer>> table;

    public KeyMap() {
        table = new HashMap<>();
    }

    //returns true if the association is successful, only fails if every possible keyboard shortcut is taken for the given state
    //state: represents state value of Root.getPortal(), positive for homeScreen = true, negative for homeScreen = false (i.e. TaskViews open or tiled)
    public boolean associate(int state, KeyCombo shortcut, Consumer function) {
        HashMap<KeyCombo, Consumer> entry = table.computeIfAbsent(state, k -> new HashMap<>());
        Consumer existing = entry.get(shortcut);
        entry.put(shortcut, function);
        if (existing != null) {
            KeyCombo next = nextAvailable(state, shortcut);
            if (next != null) {
                entry.put(next, existing);
            } else {
                entry.put(shortcut, existing);
                return false;
            }
        }
        return true;
    }

    private KeyCombo nextAvailable(int state, KeyCombo old) {
        KeyCombo current = new KeyCombo(new HashSet<>(), old.key.getChar().charAt(0));
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
            current = next(state, current);
            if (current.key.equals(old.key)) {
                //we cycled all the way around and every possible shortcut is full, just give up.
                return null;
            }
        }
    }

    private boolean testKey(int state, KeyCombo current) {
        return table.computeIfAbsent(state, k -> new HashMap<>()).containsKey(current);
    }

    private KeyCombo next(int state, KeyCombo current) {
        char old = current.key.getChar().charAt(0);
        old++;
        while (!(Character.isLetterOrDigit(old) || Character.getType(old) == Character.MATH_SYMBOL))
            old++;
        return new KeyCombo(current.functionKeys, old);
    }

    // returns "true" if the association succeeds, "false" if there are no more slots available (see above)
    // returns error message if IllegalKeyComboException occurs.
    public String associate(int state, String shortcutRegex, Consumer function) {
        try {
            return Boolean.toString(associate(state, KeyCombo.parse(shortcutRegex), function));
        } catch (IllegalKeyComboException e) {
            return e.getMessage();
        }
    }

    public void fireEvent(KeyEvent event, int state, boolean negative) {
        int biasedState = negative ? -1 * state : state;
        KeyCombo gen = new KeyCombo(new HashSet<>(), event.getCode().getChar().charAt(0));
        if (event.isControlDown())
            gen.functionKeys.add(FunctionKey.CONTROL);
        if (event.isAltDown())
            gen.functionKeys.add(FunctionKey.ALT);
        if (event.isShiftDown())
            gen.functionKeys.add(FunctionKey.SHIFT);

        HashMap<KeyCombo, Consumer> entry = table.get(biasedState);
        if (entry != null) {
            Consumer fun = entry.get(gen);
            if (fun != null) {
                fun.accept(null);
            }
        }
    }

    enum FunctionKey {
        CONTROL, ALT, SHIFT
    }

    private static class KeyCombo {
        Set<FunctionKey> functionKeys;
        KeyCode key;

        KeyCombo(Set<FunctionKey> functionKeys, char key) {
            this.functionKeys = functionKeys;
            this.key = KeyCode.getKeyCode(Character.toString(key));
        }
        
        public static KeyCombo parse(String combo) throws IllegalKeyComboException {
            HashSet<FunctionKey> functionKeys = new HashSet<>();
            char argChar = (char) 0;
            String[] keys = combo.split("\\+");
            for (String key : keys) {
                switch (key.toLowerCase().trim()) {
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
                        default:
                            if (key.length() > 1 || argChar != 0)
                                throw new IllegalKeyComboException("Illegal Key Combination Expression for Keyboard Shortcut: " + key);
                            else
                                argChar = key.charAt(0);
                }
            }
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
    }
}
