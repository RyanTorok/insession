package server;

import server.database.QueryGate;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LocalServers {

    static JSONObject lookup(String nickname) {
        try {
            ResultSet resultSet = new QueryGate().query("SELECT * FROM registered_hosts WHERE `nickname` = ?", "s", nickname);
            if (resultSet.isAfterLast())
                return null;
            resultSet.next();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("nickname", resultSet.getString("nickname"));
            jsonObject.put("ipv4", resultSet.getString("ipv4"));
            jsonObject.put("ipv6", resultSet.getString("ipv6"));
            jsonObject.put("version", resultSet.getInt("version"));
            return jsonObject;
        } catch (SQLException e) {
            return null;
        }
    }
}
