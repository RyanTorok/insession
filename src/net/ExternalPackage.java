package net;

import java.io.IOException;
import java.io.Serializable;

public class ExternalPackage implements Comparable<ExternalPackage>, Serializable {

    private static final long serialVersionUID = 40002;

    private long timeImported;
    private String nickname;

    public ExternalPackage(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public int compareTo(ExternalPackage o) {
        return Long.compare(timeImported, o.timeImported);
    }

    public String getNickname() {
        return nickname;
    }

    public Session newSession() throws IOException {
        Session session = new Session(nickname);
        session.open();
        return session;
    }

    public boolean testConnection() {
        try {
            ServerSession serverSession = new ServerSession();
            serverSession.open();
            String[] response = serverSession.callAndResponse("testexternal", nickname);
            serverSession.close();
            return response.length == 1 && response[0].equals("success");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    static class Session {

        private String nickname;
        private long token;
        private boolean open;

        Session(String nickname) {
            this.nickname = nickname;
            token = 0;
            open = false;
        }

        void open() throws IOException {
            if (open)
                return;
            ServerSession serverSession = new ServerSession();
            serverSession.open();
            String[] response = serverSession.callAndResponse("external", nickname);
            serverSession.close();
            token = Long.parseLong(response[0]);
            open = true;
        }

        void send(String message) throws IOException {
            if (!open) {
                throw new IllegalStateException("Session is not open");
            }
            ServerSession serverSession = new ServerSession();
            serverSession.open();
            serverSession.sendOnly("externalsend", Long.toString(token), message);
            serverSession.close();
        }

        String[] receive() throws IOException {
            if (!open) {
                throw new IllegalStateException("Session is not open");
            }
            ServerSession serverSession = new ServerSession();
            serverSession.open();
            String[] response = serverSession.callAndResponse("externalreceive", Long.toString(token));
            serverSession.close();
            return response;
        }

    }
}
