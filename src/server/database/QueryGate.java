package server.database;

import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.UUID;
import java.util.stream.Stream;

public class QueryGate implements AutoCloseable {
    private Connection conn;
    private ResultSet lastResults;
    private boolean open;

    public static int numColumns(String table_name) {
        QueryGate gate = new QueryGate();
        gate.connect();
        try {
            return gate.query("SELECT COUNT(*) AS  FROM information_schema.columns WHERE table_name = ?;", table_name).getInt("n");
        } catch (SQLException e) {
            return -1;
        }
    }

    void connect() {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            String url = "jdbc:derby://localhost:3306/paintbrush;create=false;";
            String username = "paintbrush";
            String password = "paintbrush";
            conn = DriverManager.getConnection(url, username, password);
            useDatabase();
        }
        catch (Exception e) {
            if (e instanceof ClassNotFoundException){
                e.printStackTrace();
                return;
            }
            //database does not exist.
            String url = "jdbc:derby://localhost:3306/paintbrush;create=true;";
            String username = "paintbrush";
            String password = "paintbrush";
            try {
                conn = DriverManager.getConnection(url, username, password);
                useDatabase();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private boolean useDatabase() throws SQLException {
        PreparedStatement statement = conn.prepareStatement("USE paintbrush_server;");
        return statement.execute();
    }

    void disconnect() {
        try {
            conn.close();
            conn = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        disconnect();
    }

    public ResultSet query(String sql, String types, Object... arguments) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(sql);
        if (types.length() != arguments.length) {
            throw new SQLException("unmatching length of arguments and types");
        }
        for (int i = 0; i < arguments.length; i++) {
            switch (types.charAt(i)) {
                case 'i': statement.setInt(i + 1, Integer.parseInt(arguments[i].toString())); break;
                case 'u': {
                    UUID uuid = arguments[i] instanceof UUID ? (UUID) arguments[i] : UUID.fromString(arguments[i].toString());
                    ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
                    byteBuffer.putLong(uuid.getMostSignificantBits());
                    byteBuffer.putLong(uuid.getLeastSignificantBits());
                    statement.setBytes(i + 1, byteBuffer.array());
                    break;
                }
                case 'h': statement.setShort(i + 1, Short.parseShort(arguments[i].toString())); break;
                case 'l': statement.setLong(i + 1, Long.parseLong(arguments[i].toString())); break;
                case 'f': statement.setFloat(i + 1, Float.parseFloat(arguments[i].toString())); break;
                case 'd': statement.setDouble(i + 1, Double.parseDouble(arguments[i].toString())); break;
                case 't': statement.setTimestamp(i + 1, (Timestamp) arguments[i]); break;
                case 's':
                default: statement.setString(i + 1, arguments[i].toString());
            }
        }
        if (!open)
            connect();
        return statement.executeQuery();
    }
}
