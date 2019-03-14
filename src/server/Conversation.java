package server;

import java.util.concurrent.BlockingQueue;

public class Conversation {

    static final int QUEUE_SIZE = 64;
    private final Long alice;
    private final Long bob;
    private final BlockingQueue<String> aliceQueue;
    private BlockingQueue<String> bobQueue;

    public Conversation(Long alice, Long bob, BlockingQueue<String> aliceQueue) {
        this.alice = alice;
        this.bob = bob;
        this.aliceQueue = aliceQueue;
        open();
    }

    private void open() {

    }

    void send(String s) {

    }

    String receive() {
        return null;
    }
}
