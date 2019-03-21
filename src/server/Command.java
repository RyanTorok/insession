package server;
import org.json.JSONArray;
import org.json.JSONObject;
import server.database.DatabaseUtils;
import server.database.QueryGate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.UUID;

public abstract class Command {


    private Long executorId;
    private String[] arguments;
    private String opcode;


    public Command(String[] arguments) {
        this.executorId = executorId;
        this.arguments = arguments;
        this.opcode = arguments[0];
    }

    public static Command getAsType(String name, String[] arguments, Long executorId) {
        Command c = getAsType(name, arguments);
        c.executorId = executorId;
        return c;
    }

    private static Command getAsType(String name, String[] arguments) {
        switch (name) {
            case "connectiontest": return new ConnectionTest(arguments);
            case "hostexists": return new HostExists(arguments);
            case "registerhost": return new RegisterHost(arguments);
            default: return null;
        }
    }

    protected Long getExecutorId() {
        return executorId;
    }

    protected String getExecutorNickname() throws SQLException {
        return getServerNicknameByID(getExecutorId());
    }

    protected String getServerNicknameByID(long id) throws SQLException {
        ResultSet resultSet = new QueryGate().query("SELECT `nickname` FROM registered_hosts WHERE `id` = ?", "l", id);
        if (resultSet.isAfterLast())
            return null;
        resultSet.next();
        return resultSet.getString("nickname");
    }

    protected Long getServerID(String nickname) throws SQLException {
        ResultSet resultSet = new QueryGate().query("SELECT `id` FROM registered_hosts WHERE `id` = ?", "s", nickname);
        if (resultSet.isAfterLast())
            return null;
        resultSet.next();
        return resultSet.getLong("id");
    }

    protected String getArgumentAsString(int index) {
        index += 3;
        if (index < 0 || index >= arguments.length)
            return "";
        return arguments[index];
    }

    protected Integer getArgumentAsInteger(int index) throws WrongArgumentTypeException {
        String arg = getArgumentAsString(index);
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            throw new WrongArgumentTypeException("expected integer at index " + index + ", got " + arg);
        }
    }

    protected Long getArgumentAsLong(int index) throws WrongArgumentTypeException {
        String arg = getArgumentAsString(index);
        try {
            return Long.parseLong(arg);
        } catch (NumberFormatException e) {
            throw new WrongArgumentTypeException("expected integer at index " + index + ", got " + arg);
        }
    }

    protected Double getArgumentAsDouble(int index) throws WrongArgumentTypeException {
        String arg = getArgumentAsString(index);
        try {
            return Double.parseDouble(arg);
        } catch (NumberFormatException e) {
            throw new WrongArgumentTypeException("expected double at index " + index + ", got " + arg);
        }
    }

    protected Boolean getArgumentAsBoolean(int index) throws WrongArgumentTypeException {
        String arg = getArgumentAsString(index);
        try {
            return Boolean.getBoolean(arg.toLowerCase());
        } catch (Exception e) {
            throw new WrongArgumentTypeException("expected boolean at index " + index + ", got " + arg);
        }
    }

    protected UUID getArgumentAsUUID(int index) throws WrongArgumentTypeException {
        String arg = getArgumentAsString(index);
        try {
            return UUID.fromString(arg);
        } catch (IllegalArgumentException e) {
            throw new WrongArgumentTypeException("expected uuid at index " + index + ", got " + arg);
        }
    }

    protected UUID optionalArgumentAsUUID(int index) {
        //TODO don't use exceptions as control flow
        try {
            return getArgumentAsUUID(index);
        } catch (WrongArgumentTypeException e) {
            return null;
        }
    }

    abstract String execute() throws WrongArgumentTypeException, SQLException;

    static String makeReturn(Object... values) {
        StringBuilder combined = new StringBuilder();
        for (Object o : values) {
           String str = o.toString();
           combined.append(URLEncoder.encode(str, StandardCharsets.UTF_8)).append(" ");
        }
        return combined.toString();
    }

    static String makeReturn(ResultSet resultSet) throws SQLException {
        return makeReturn(parseResultSet(resultSet));
    }

    private static JSONArray parseResultSet(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        JSONArray array = new JSONArray();
        for (int i = 0; !resultSet.isAfterLast(); i++) {
            JSONObject object = new JSONObject();
            for (int j = 0; j < metaData.getColumnCount(); j++) {
                object.put(metaData.getColumnLabel(j), resultSet.getObject(j).toString());
            }
            array.put(object);
            resultSet.next();
        }
        return array;
    }

    protected String allQuestionMarks(String table_name, boolean paren) {
        return DatabaseUtils.questionMarks(QueryGate.numColumns(table_name), paren);
    }
}