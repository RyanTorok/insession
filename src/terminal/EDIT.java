package terminal;

import main.UtilAndConstants;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class EDIT extends Command {
    @Override
    protected TerminalRet execute(ArrayList<Token> tokens) throws TerminalException {
        assertTokenCount(2, tokens, "file to edit");
        File f = Address.parse(tokens.get(1).getTokenLabel(), true, false, false, true, false);
        try {
            if (UtilAndConstants.getOperatingSystem().equals("win")) {
                Runtime.getRuntime().exec("notepad.exe " + f.getAbsolutePath());
            } else if (UtilAndConstants.getOperatingSystem().equals("mac")) {
                Runtime.getRuntime().exec("open " + f.getAbsolutePath());
            } else if (UtilAndConstants.getOperatingSystem().equals("linux")) {
                Runtime.getRuntime().exec("gedit " + f.getAbsolutePath());
            } else {
                throw new TerminalException("edit - Your operating system does not support this command.");
            }
        } catch (IOException e) {
            throw new TerminalException("edit - an error occurred when opening file '" + f.getName() + "'.");
        }
        return new TerminalRet("");
    }
}
