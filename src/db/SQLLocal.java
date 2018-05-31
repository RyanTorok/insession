package db;

import java.sql.*;

public class SQLLocal {
    static Connection conn;

    static void establish() {
        String url = "jdbc:mysql://localhost:3306/";
        String username = "student";
        String password = "student";
        String driver = "com.mysql.jdbc.Driver";
        String dbName = "userdata";
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            conn = DriverManager.getConnection(url + dbName, username, password);
        } catch (SQLException e){
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
