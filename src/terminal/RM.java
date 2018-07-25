package terminal;

import java.io.File;
import java.util.ArrayList;

public class RM extends Command {
    @Override
    protected TerminalRet execute(ArrayList<Token> tokens) throws TerminalException {
        if (tokens.size() != 2)
            throw new TerminalException("wrong number of arguments for command 'rm', expected name of new directory");
        String fn = tokens.get(1).getTokenLabel();
        File f = new File(System.getProperty("user.dir") + File.separator + fn);
        if (!f.exists()) {
            String message;
            if (fn.indexOf(File.separator) > -1) {
                int index = fn.lastIndexOf(File.separator);
                message = "rm - file '" + fn.substring(index + 1) + "' does not exist in directory " + fn.substring(0, index);
            }
            else message = "rm - file '" + fn + "' does not exist in the current directory.";
            throw new TerminalException(message);
        }

        boolean success = f.delete();
        if (!success) {
            throw new TerminalException("rm - an error occurred when deleting file '" + Address.parse(fn, true, false, false, false, false).getName() + "'.");
        }

        return new TerminalRet("");
    }
}
