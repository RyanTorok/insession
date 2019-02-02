package server;

public class Close extends AnonymousCommand {

    public Close(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() {
        return "done";
    }
}
