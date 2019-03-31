package localserver;

import main.PasswordManager;
import terminal.Address;

import java.io.*;

public class ServerInfo implements Serializable {
    static final long serialVersionUID = 100000L;

    private final long id;
    private final String nickname;
    private transient String password;

    public ServerInfo(long id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public long getId() {
        return id;
    }

    static ServerInfo read() {
        File ser = new File(Address.fromRootAddr("serverinfo.ser"));
        if (!ser.exists())
            return null;
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(ser));
            return (ServerInfo) in.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            e.printStackTrace();
            //for debug only TODO remove
            ServerInfo test = new ServerInfo(0, "test");
            test.write();
            return test;
        }
    }

    void write() {
        try {
            File dest = new File(Address.fromRootAddr("serverinfo.ser"));
            if (!dest.exists()) {
                boolean b = dest.createNewFile();
                if (!b) {
                    throw new IOException("an error occurred creating server info file.");
                }
            }
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(dest));
            out.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPassword() {
        return password;
    }

    void setPassword(String pwd) {
        password = pwd;
    }
}
