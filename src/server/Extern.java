package server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Extern extends Command {

    public Extern(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException {

        BlockingQueue<String> sendQueue = new ArrayBlockingQueue<>(Conversation.QUEUE_SIZE);

        return "error : not implemented yet";
    }
}
