package main;

import terminal.Address;

import java.io.*;

public class DefaultUser implements Serializable {

    static long serialVersionUID = 43L;

    private String defaultUser; //filename

    public void setDefaultUser(String defaultUser) {
        this.defaultUser = defaultUser;
    }

    public String getDefaultUser() {
        return defaultUser;
    }

    public String read() {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(Address.fromRootAddr("default.ser"))));
            defaultUser = (String) in.readObject();
        } catch (Exception e) {
            defaultUser = null;
        }
        return getDefaultUser();
    }

    //writes the default user filename to the disk and returns true if the write is successful
    public boolean write() {
        try {
            File f = new File(Address.fromRootAddr("default.ser"));
            if (!f.exists()) {
                boolean success = f.createNewFile();
                if (!success) {
                    throw new IOException("Failed to create default user file.");
                }
            }
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
            out.writeObject(this);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
