package localserver;

import main.PasswordManager;
import main.User;
import localserver.database.DatabaseUtils;
import localserver.database.QueryGate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Base64;

public class CreateAccount extends AnonymousCommand {

    public CreateAccount(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws SQLException {
        String username = getArgumentAsString(0),
                password = getArgumentAsString(1),
                first = getArgumentAsString(2),
                last = getArgumentAsString(3),
                email = getArgumentAsString(4),
                activationKey = getArgumentAsString(5);

        password = URLDecoder.decode(password, StandardCharsets.UTF_8);
        //decode base64
        password = new String(Base64.getDecoder().decode(password), StandardCharsets.UTF_8);
        PasswordManager.PasswordCombo combo = PasswordManager.newGen(password);
        if (combo == null)
            return "error : security exception thrown";

        QueryGate gate = new QueryGate();
        StringBuilder class_regex = new StringBuilder();

        //delete expired activation keys
        gate.update("DELETE FROM account_tokens WHERE expires < CURRENT_TIMESTAMP");

        long newId = IDAllocator.getLong();

        //check activation key
        boolean akExists = activationKey.length() != 0;
        ResultSet classes = null;
        if (akExists) {
            classes = gate.query("SELECT class, type FROM account_tokens WHERE token = ?", "s", activationKey);
            if (classes.isAfterLast())
                return "error : unrecognized or expired activation key";
        }

        User newUser = new User(newId, username, first, null, last, email, new Timestamp(System.currentTimeMillis()));
        ObjectOutputStream out = null;
        byte[] bytes = new byte[]{};
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            out = new ObjectOutputStream(bos);
            out.writeObject(newUser);
            out.flush();
            bytes = bos.toByteArray();
        } catch (IOException e) {
            out = null;
        }

        //add the user to the database
        String serfile = Base64.getEncoder().encodeToString(bytes);
        System.out.println(serfile);
        gate.update("INSERT INTO users (id, username, `password`, first, last, email, salt, serfile) VALUES " + DatabaseUtils.questionMarks(8, true)+ ";", "lsssssss", newId, username, Base64.getEncoder().encodeToString(combo.getEncryptedPassword()), first, last, email, Base64.getEncoder().encodeToString(combo.getSalt()), serfile);

        //link activation key
        if (akExists) {
            while (!classes.isAfterLast()) {
                String classId = classes.getString("class");
                class_regex.append(classId).append(" ").append(classes.getInt("type"));
                gate.update("INSERT INTO schedule VALUE(?, ?);", "lu", newId, classId);
            }
        }

        //prevent the activation key from being reused
        gate.update("DELETE FROM account_tokens WHERE token = ?", "s", activationKey);

        return newId + " " + class_regex.toString();
    }
}
