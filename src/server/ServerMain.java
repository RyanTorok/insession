package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class ServerMain {

    public static final int PORT = 6520;

    public static void main(String[] args) {
        ServerSocket incoming = null;
        try {
            incoming = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        while (true) {
            Socket client = null;
            try {
                client = incoming.accept();
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
            Thread handleClient = new Thread(() -> {
                boolean close = false;
                while (!close) {
                    ServerCall call = null;
                    try {
                        String strIn = in.readLine();
                        if (strIn != null)
                            call = new ServerCall(URLDecoder.decode(strIn, StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        out.println("error : server got bad input on port " + PORT + "\n");
                    }
                    String result = call != null ? call.execute() : "";
                    out.println(result);
                }
            });
            handleClient.start();
        }
    }

    static String keyGen() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[128];
        random.nextBytes(bytes);
        byte[] encoded = Base64.getEncoder().encode(bytes);
        return new String(encoded, StandardCharsets.UTF_8);
    }
}
