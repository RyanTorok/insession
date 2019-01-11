package server;

import main.Root;

public class RunAll {
    public static void main(String[] args) {
        Thread runServer = new Thread(() -> ServerMain.main(new String[]{}));
        Thread runClient = new Thread(() -> Root.main(new String[]{}));
        runServer.start();
        runClient.start();
    }
}
