package localserver;

import main.PasswordManager;

import java.io.IOException;

public class GenerateTestHost {
    public static void main(String[] args) {
        try {
            CentralServerSession session = new CentralServerSession();
            String password = "test";
            PasswordManager.PasswordCombo combo = PasswordManager.newGen(password);
            if (combo == null) {
                System.out.println("Security error");
                System.exit(-1);
            }
            Long result = session.registerHost("test", "localhost", "localhost", 0, combo.getEncryptedPassword());
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Connection error");
            System.exit(-1);
        }
    }
}
