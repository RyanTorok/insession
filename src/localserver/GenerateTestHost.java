package localserver;

import main.PasswordManager;

import java.io.IOException;
import java.util.Base64;

public class GenerateTestHost {
    public static void main(String[] args) {
        try {
            CentralServerSession session = new CentralServerSession();
            String password = "test";
            PasswordManager.PasswordCombo combo = PasswordManager.newGenLocal(password, "test");
            if (combo == null) {
                System.out.println("Security error");
                System.exit(-1);
            }
            String nickname = "test";
            System.out.println(Base64.getEncoder().encodeToString(combo.getEncryptedPassword()));
            Long id = session.registerHost(nickname, "localhost", "localhost", 0, combo.getEncryptedPassword());
            ServerInfo info = new ServerInfo(id, nickname);
            info.write();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Connection error");
            System.exit(-1);
        }
    }
}
