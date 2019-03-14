package main;

import localserver.ServerMain;

public class RunAll {
    public static void main(String[] args) {
        Thread runServer = new Thread(() -> server.ServerMain.main(new String[]{}));
        Thread runLocalServer = new Thread(() -> ServerMain.main(new String[]{}));
        Thread runClient = new Thread(() -> Root.main(new String[]{}));
        runServer.start();
        runLocalServer.start();
        runClient.start();
    }
}
