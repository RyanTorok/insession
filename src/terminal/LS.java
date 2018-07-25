package terminal;

import gui.Terminal;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LS extends Command {

    @Override
    protected TerminalRet execute(ArrayList<Token> input) throws TerminalException {
        int depth;
        Stream<Path> walk;
        try {
            depth = input.size() <= 1 ? 1 : Integer.parseInt(input.get(1).getTokenLabel());
        } catch (NumberFormatException e) {
            throw new TerminalException("unexpected token: " + input.get(1).getTokenLabel());
        }
        try {
            Path path = FileSystems.getDefault().getPath(System.getProperty("user.dir"));
            walk = Files.walk(path, depth, FileVisitOption.FOLLOW_LINKS);
        } catch (SecurityException e) {
            return new TerminalRet("ls: Permission denied for folder: " + System.getProperty("user.dir"));
        }
        catch (IOException e) {
            return new TerminalRet("ls: an error occured when parsing folder: " + System.getProperty("user.dir"));
        }
        List<Path> list = walk.collect(Collectors.toList());
        String out = "";
        boolean current = true;
        for (Path p: list) {
            if (current) {
                current = false;
                continue;
            }
            out += p.toFile().getName() + "      ";
        }
        return new TerminalRet(out);
    }
}