package gui;

import classes.PhoneNumber;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Created by 11ryt on 6/7/2017.
 */
public class PhoneField extends JFormattedTextField {

    private PhoneNumber pn;

    public PhoneField(int type) {
        this.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                modifyPN(type);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                insertUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
    }

    public PhoneField(PhoneNumber pn) {
        this.setText(pn.returnAsString());
    }

    private boolean hasNumbers() {
        char[] cs = getText().toCharArray();
        for (char c : cs) {
            if (47 < c && c < 58)
                return true;
        }
        return false;
    }

    private void modifyPN(int type) {
        Runnable subThread = () -> {
            if (!hasNumbers()) {
                setText("");
            } else if (getText().matches("[0-9][0-9][0-9]")) {
                setText("(" + getText() + ")");
            } else if (getText().matches("[(][0-9][0-9][0-9]")) {
                setText(getText() + ")");
            } else if (getText().matches("[(][0-9][0-9][0-9][)][0-9][0-9][0-9]"))
                setText(getText() + "-");
            else if (getText().matches("[(][0-9][0-9][0-9][)][0-9][0-9][0-9][-]][0-9][0-9][0-9]")) ;
            pn = new PhoneNumber(getText(), type);
            validate();
        };
        SwingUtilities.invokeLater(subThread);
    }
}
