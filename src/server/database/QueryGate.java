package server.database;

import java.nio.ByteBuffer;
import java.sql.*;
import java.util.UUID;

public class QueryGate implements AutoCloseable {
    private static Connection conn;
    private ResultSet lastResults;
    private boolean open;

    static {
        connect();
    }

    public QueryGate() {
        open = true;
    }

    public static int numColumns(String table_name) {
        try {
            return new QueryGate().query("SELECT COUNT(*) AS  FROM information_schema.columns WHERE table_name = ?;", table_name).getInt("n");
        } catch (SQLException e) {
            return -1;
        }
    }

    private static void connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/central_server";
            String username = "central_server_java";
            String password = "paintbrush";
            conn = DriverManager.getConnection(url, username, password);
            useDatabase();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void useDatabase() throws SQLException {
        PreparedStatement statement = conn.prepareStatement("USE central_server;");
        statement.execute();
    }

    private static void disconnect() {
        try {
            conn.close();
            conn = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet query(String sql, String types, boolean update, Object... arguments) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(sql);
        if (types.length() != arguments.length) {
            throw new SQLException("unmatching length of arguments and types");
        }
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] == null) {
                int type;
                switch (types.charAt(i)) {
                    case 'i': type = Types.INTEGER; break;
                    case 'u': type = Types.BINARY; break;
                    case 'h': type = Types.SMALLINT; break;
                    case 'l': type = Types.BIGINT; break;
                    case 'f': type = Types.FLOAT; break;
                    case 'd': type = Types.DOUBLE; break;
                    case 't': type = Types.TIMESTAMP; break;
                    case 's':
                    default: type = Types.VARCHAR;
                }
                statement.setNull(i + 1, type);
                continue;
            }
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
        if (update) {
            statement.executeLargeUpdate();
            statement.executeUpdate();
            return null;
        }
        return statement.executeQuery();
    }

    public ResultSet query(String sql) throws SQLException {
        return query(sql, "");
    }

    public void update(String sql) throws SQLException {
        update(sql, "");
    }

    public void update(String sql, String types, Object... arguments) throws SQLException {
        query(sql, types, true, arguments);
    }

    public ResultSet query(String sql, String types, Object... arguments) throws SQLException {
        return query(sql, types, false, arguments);
    }

    @Override
    public void close() throws Exception {
        open = false;
    }
}
