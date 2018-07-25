package terminal;

import main.UtilAndConstants;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class OPEN extends Command {
    @Override
    protected TerminalRet execute(ArrayList<Token> tokens) throws TerminalException {
        assertTokenCount(2, tokens, "file to open");
        File f = Address.parse(tokens.get(1).getTokenLabel(), true, false, false, true, false);
        try {
            if (UtilAndConstants.getOperatingSystem().equals("win")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + f.toURI());
            } else if (UtilAndConstants.getOperatingSystem().equals("mac")) {
                Runtime.getRuntime().exec("open " + f.getAbsolutePath());
            } else if (UtilAndConstants.getOperatingSystem().equals("linux")) {
                Runtime.getRuntime().exec("xdg-open " + f.getAbsolutePath());
            } else {
                throw new TerminalException("open - Your operating system does not support this command.");
            }
        } catch (IOException e) {
            throw new TerminalException("open - an error occurred when opening file '" + f.getName() + "'.");
        }
        return new TerminalRet("");
    }
}
