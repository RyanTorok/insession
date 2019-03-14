package main;

import localserver.ServerMain;

public class Servers {
    public static void main(String[] args) throws InterruptedException {
        Thread runServer = new Thread(() -> server.ServerMain.main(new String[]{}));
        Thread runLocalServer = new Thread(() -> ServerMain.main(new String[]{}));
        runServer.start();
        runLocalServer.start();
    }
}
