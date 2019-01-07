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

public abstract class Command {


    private String[] arguments;
    private String opcode;


    public Command(String[] arguments) {
        this.arguments = arguments;
        this.opcode = arguments[0];
    }

    public static Command getAsType(String name, String[] arguments) {
        switch (name) {
            case "weather": return new Weather(arguments);
            default: return null;
        }
    }

    protected String getArgumentAsString(int index) {
        index += 3;
        if (index < 0 || index >= arguments.length)
            return null;
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


    abstract String execute() throws WrongArgumentTypeException;

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