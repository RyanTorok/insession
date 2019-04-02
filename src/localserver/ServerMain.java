package localserver;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;
import java.util.concurrent.*;

public class ServerMain {

    public static final int PORT = 6520;
    private static final int OPERATION_TIMEOUT_MILLIS = 10000;
    private static final int SESSION_TIMEOUT_MILLIS = 60000;
    private static ServerInfo environment;

    public static void main(String[] args) {
        //who am I?
        environment = ServerInfo.read();
        Scanner readPassword = new Scanner(System.in);
        System.out.println("Password: ");
        String pwd = "test"; //readPassword.nextLine();
        environment.setPassword(pwd);
        //initialize link with central server
        ExternalListener listener = new ExternalListener();
        try {
            System.out.print("performing connection test: ");
            CentralServerSession session = new CentralServerSession();
            boolean test = session.connectionTest();
            if (!test) {
                System.out.println("failure\nError: unable to establish a connection with the central server. Please check your internet connection and try again.");
                System.exit(1);
            }
            System.out.println("success");
            session.close();
        } catch (IOException e) {
            System.out.println("failure\nError: unable to establish a connection with the central server. Please check your internet connection and try again.");
            System.exit(1);
        }
        Thread thread = new Thread(listener);
        thread.setName("Local Domain Server - Polling");
        thread.start();

        //open server for client connections
        ServerSocket incoming = null;
        try {
            //incoming = SSLServerSocketFactory.getDefault().createServerSocket(PORT);
            incoming = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        while (true) {
            Socket client = null;
            try {
                client = incoming.accept();
                client.setSoTimeout(OPERATION_TIMEOUT_MILLIS);
            } catch (IOException e) {
                System.out.println("Error encountered when receiving client socket:");
                e.printStackTrace();
                continue;
            }
            PrintWriter out_ = null;
            BufferedReader in_ = null;
            try {
                out_ = new PrintWriter(client.getOutputStream(), true);
                in_ = new BufferedReader(new InputStreamReader(client.getInputStream()));
            } catch (IOException e) {
                System.out.println("Error encountered when receiving client socket:");
                e.printStackTrace();
                continue;
            }
            final PrintWriter out = out_;
            final BufferedReader in = in_;
            Runnable handleClient = ()-> {
                boolean close = false;
                while (!close) {
                    ServerCall call = null;
                    try {
                        String strIn = in.readLine();
                        if (strIn != null)
                            call = new ServerCall(strIn);
                        else return;
                    } catch (SocketTimeoutException e) {
                        return;
                    } catch (IOException e) {
                        out.println("error : server got bad input on port " + PORT + "\n");
                    }
                    String result = call != null ? call.execute() : "";
                    if (call != null && call.requestedClose()) {
                        close = true;
                    }
                    out.println(result);
                }
            };
            NamedThreadFactory factory = new NamedThreadFactory("Handle Client " + incoming.getInetAddress());
            ExecutorService execution = Executors.newSingleThreadExecutor(factory);
            Future<?> submit = execution.submit(handleClient);
            try {
                submit.get(SESSION_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
        }
    }

    static String keyGen() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        byte[] encoded = Base64.getEncoder().encode(bytes);
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static ServerInfo getEnvironment() {
        return environment;
    }
}
