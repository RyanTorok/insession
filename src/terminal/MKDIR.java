package terminal;

import java.io.File;
import java.util.ArrayList;

public class MKDIR extends Command{
    @Override
    protected TerminalRet execute(ArrayList<Token> tokens) throws TerminalException {
        if (tokens.size() != 2)
            throw new TerminalException("wrong number of arguments for command 'mkdir', expected name of new directory");
        String directoryName = tokens.get(1).getTokenLabel();

        File f  = new File(System.getProperty("user.dir") + File.separator + directoryName);
        if (f.exists()) {
            throw new TerminalException("mkdir - cannot create directory '" + directoryName + "' because a file with that name already exists.");
        } else {
            boolean b = f.mkdirs();
            if (!b)
                throw new TerminalException("mkdir - an error occurred when creating direcory '" + directoryName + "'.");
        }
        return new TerminalRet("", false, false);
    }
}
