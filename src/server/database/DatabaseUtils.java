package server.database;

public class DatabaseUtils {
    public static String questionMarks(int n, boolean paren) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < n; i++) {
            s.append("?");
            if (i < n - 1)
                s.append(", ");
        }
        if (paren)
            return "(" + s.toString() + ")";
        return s.toString();
    }
}
