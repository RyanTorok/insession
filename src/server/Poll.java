package server;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class Poll extends Command {

    private static final HashMap<Long, Queue<String>> availablePolls;
    private static final int QUEUE_SIZE = 128;

    static {
        availablePolls = new HashMap<>();
        //TODO automate this, this part here is just for testing purposes
        availablePolls.put(6049242897348703150L, new LinkedList<>());
    }

    public Poll(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() {
        Long me = getExecutorId();
        Queue<String> requests = availablePolls.get(me);
        if (requests.isEmpty())
            return "done";
        return requests.poll();
    }

    public static void request(Long serverId, Long token, String sourceNickname, String commandName, Object... arguments) {
        Queue<String> polls = availablePolls.get(serverId);
        if (polls == null)
            throw new IllegalArgumentException("unknown host error");
        StringBuilder command = new StringBuilder(token + " " + sourceNickname + " " + commandName);
        for (Object arg : arguments) {
            command.append(' ');
            command.append(arg.toString());
        }
        polls.add(command.toString());
    }

    static void registerHost(Long serverId) {
        availablePolls.put(serverId, new LinkedBlockingQueue<>(QUEUE_SIZE));
    }
}
