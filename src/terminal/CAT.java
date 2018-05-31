package terminal;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class CAT extends Command {
    @Override
    protected TerminalRet execute(ArrayList<Token> tokens) throws TerminalException {
        assertTokenCount(2, tokens, "file name");
        File f = Address.parse(tokens.get(1).getTokenLabel(), true, false, false, true, false);
        Scanner reader;
        try {
            reader = new Scanner(f);
        } catch (FileNotFoundException e) {
            throw new TerminalException("cat - an error occurred when reading the file " + f.getName());
        }
        String in = "";
        while (reader.hasNextLine()) {
            if (in.length() != 0)
                in += "\n";
            in += reader.nextLine();
        }
        return new TerminalRet(in, false, false);
    }
}
