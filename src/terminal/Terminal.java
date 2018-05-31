/*package terminal;

import filesystem.FileSystemParser;
import javafx.scene.paint.Color;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class Terminal {

    private FileSystemParser fileSystemParser;
    private Tokenizer tok;
    private BufferedOutputStream writer;

    public void execute(String command) throws IOException {
        try {
            getTok().loadCommand(command);
        } catch (TerminalException e) {
            e.printStackTrace();
        }
        int tokensIndex = 0;
        for (Token t : getTok().getTokens()) {
            switch (t.getType()) {
                /*case COMMAND: {
                    try {
                        if (tokensIndex == getTok().getTokens().size())
                           Command.parseCommand(t).execute(null, this);
                        else Command.parseCommand(t).execute(getTok().getTokens().subList(tokensIndex + 1, getTok().getTokens().size()), this);
                    } catch (TerminalException e) {
                        output("Error: " + e.getMessage(), Color.RED);
                    }
                }
                tokensIndex++;
            }
        }
    }

    public Terminal(FileSystemParser fileSystemParser, Terminal gui) {
        this.fileSystemParser = fileSystemParser;
        tok = new Tokenizer();
        writer = gui.getOutputStream();
    }

    public void output(String s, Color preferredColor) throws IOException {
        char[] arr = s.toCharArray();
        for (char c :
                arr) {
            writer.write(c);
            //TODO implement coloring writer
        }
    }

    public void output(String s) throws IOException {
        output(s, null);
    }

    public FileSystemParser getFileSystemParser() {
        return fileSystemParser;
    }

    public void setFileSystemParser(FileSystemParser fileSystemParser) {
        this.fileSystemParser = fileSystemParser;
    }

    public Tokenizer getTok() {
        return tok;
    }

    public void setTok(Tokenizer tok) {
        this.tok = tok;
    }

    public BufferedOutputStream getWriter() {
        return writer;
    }
}
*/