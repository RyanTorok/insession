package net;

import org.json.JSONException;
import org.json.JSONObject;

public class Domain {
    private String nickname;
    private String ipv4;
    private String ipv6;
    private int version;

    public Domain(String nickname, String ipv4, String ipv6, int version) {

        this.nickname = nickname;
        this.ipv4 = ipv4;
        this.ipv6 = ipv6;
        this.version = version;
    }

    public static Domain fromJSONObject(JSONObject object) {
        if (object == null)
            return null;
        try {
            return new Domain(object.getString("nickname"), object.getString("ipv4"), object.getString("ipv6"), object.getInt("version"));
        } catch (JSONException e) {
            System.err.println("Cannot create Domain from JSONObject. Incorrect object format");
            return null;
        }
    }

    public String getNickname() {
        return nickname;
    }

    public String getIpv4() {
        return ipv4;
    }

    public String getIpv6() {
        return ipv6;
    }

    public int getVersion() {
        return version;
    }
}
