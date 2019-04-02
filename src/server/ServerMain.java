package server;

import localserver.NamedThreadFactory;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.*;

public class ServerMain {

    public static final int PORT = 6521;
    public static final int POLLING_PORT = 6523;
    private static final int OPERATION_TIMEOUT_MILLIS = 10000;
    private static final int SESSION_TIMEOUT_MILLIS = 30000;

    public static void main(String[] args) {
        ServerSocket incoming = null;
        try {
            //incoming = SSLServerSocketFactory.getDefault().createServerSocket(PORT);
            incoming = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        ServerSocket incomingPolls = null;
        try {
            //incomingPolls = SSLServerSocketFactory.getDefault().createServerSocket(POLLING_PORT);
            incomingPolls = new ServerSocket(POLLING_PORT);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        runService(incomingPolls, true);
        runService(incoming, false);
    }

    private static void runService(ServerSocket incoming, boolean pollsOnly) {
        Runnable service = () -> {
            while (true) {
                Socket client = null;
                try {
                    client = incoming.accept();
                    client.setSoTimeout(OPERATION_TIMEOUT_MILLIS);
                } catch (IOException e) {
                    System.err.println("Error encountered when receiving client socket:");
                    e.printStackTrace();
                    continue;
                }
                PrintWriter out_ = null;
                BufferedReader in_ = null;
                try {
                    out_ = new PrintWriter(client.getOutputStream(), true);
                    in_ = new BufferedReader(new InputStreamReader(client.getInputStream()));
                } catch (IOException e) {
                    System.err.println("Error encountered when receiving client socket:");
                    e.printStackTrace();
                    continue;
                }
                final PrintWriter out = out_;
                final BufferedReader in = in_;
                Runnable handleClient = () -> {
                    boolean close = false;
                    boolean closableWithoutAuth = true;
                    while (!close) {
                        ServerCall call = null;
                        try {
                            String strIn = in.readLine();
                            if (strIn != null)
                                call = new ServerCall(strIn, closableWithoutAuth, pollsOnly);
                            else return;
                        } catch (SocketTimeoutException e) {
                            return;
                        } catch (IOException e) {
                            out.println("error : server got bad input on port " + PORT + "\n");
                        }
                        String result = call != null ? call.execute() : "";
                        closableWithoutAuth = call == null ? closableWithoutAuth : call.isClosableWithoutAuth();
                        if (call != null && call.requestedClose()) {
                            close = true;
                        }
                        out.println(result);
                    }
                };
                NamedThreadFactory factory = new NamedThreadFactory("Central Server Handle Client");
                ExecutorService execution = Executors.newSingleThreadExecutor(factory);
                Future<?> submit = execution.submit(handleClient);
                if (!pollsOnly) {
                    try {
                        submit.get(SESSION_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        //TODO somehow restrict this IP address for some length of time to protect against DDoS attacks
                        e.printStackTrace();
                    }
                }
            }
        };
        NamedThreadFactory factory = new NamedThreadFactory("Central Server Dispatch - Polling: " + pollsOnly);
        ExecutorService execution = Executors.newSingleThreadExecutor(factory);
        execution.submit(service);
    }

    static String keyGen() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        byte[] encoded = Base64.getEncoder().encode(bytes);
        return new String(encoded, StandardCharsets.UTF_8);
    }
}
