package terminal;

import java.io.File;
import java.nio.file.Paths;

public class Address {

    public static final File root_addr = new File(System.getProperty("user.dir"));
    

    public static File parse(String dest, boolean mustExist, boolean cannotExist, boolean isDirectory, boolean notDirectory, boolean executable) throws TerminalException {
        if (mustExist && cannotExist) {
            throw new TerminalException("file parser - argument error in must and cannot exist flags");
        }
        if (isDirectory && notDirectory)
            throw new TerminalException("file parser - argument error in is directory and is not directory flags.");
        String[] split = dest.split(File.separator);
        File current = new File(System.getProperty("user.dir"));
        int ind = 0;
        for (String fn : split) {
            if (fn.equals("")) {
                if (ind == split.length - 1) break;
                else throw new TerminalException("file parser - cannot parse consecutive file separators.");
            }
            if (fn.equals(".."))
                current = current.getParentFile();
            else if (fn.equals(".")) {
                ind++;
                continue;
            }
            else
                current = new File(current.getPath() + File.separator + fn);
            if (current == null) {
                throw new TerminalException("file parser - undefined file address: " + dest);
            }
            if (!current.exists() && mustExist)
                throw new TerminalException("file parser - file or directory not found: '" + current.getName() + "'.");
            ind++;
        }
        if (current.exists() && cannotExist) {
            throw new TerminalException("file parser - file " + current.getPath() + " already exists");
        }
        if (current.isDirectory() && notDirectory) {
            throw new TerminalException("file parser - invalid file address '" + current.getName() + "': is a directory");
        }
        if (!current.isDirectory() && isDirectory) {
            throw new TerminalException("file parser - invalid address '" + current.getName() + "': is not a directory.");
        }
        if (executable && !current.canExecute()) {
            throw new TerminalException("file parser - file '" + current.getName() + "' is not executable.");
        }
        if (Paths.get(current.toURI()).startsWith(Paths.get(root_addr.toURI()))) {
            return current;
        } else {
            throw new TerminalException("file parser - permission denied for " + current.getPath() + ".");
        }
    }
}
