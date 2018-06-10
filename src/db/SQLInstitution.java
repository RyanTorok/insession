package db;

import exceptions.OfflineException;
import main.UtilAndConstants;
import terminal.Address;

import java.io.*;
import java.sql.SQLException;

public class SQLInstitution {

    private static String url;

    public static void connect() throws OfflineException {
        if (url == null) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(Address.root_addr + File.separator + "src" + File.separator + "main" + File.separator + "LocalInitializationSchema.pbr"));
                StringBuffer sb = new StringBuffer();
                String nextLine = reader.readLine();
                int commentIndex = nextLine.indexOf("//");
                nextLine = nextLine.substring(0, commentIndex).trim();
                SQLMaster.getInstitutionURL(nextLine);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                throw new OfflineException("Could not access the institution database.");
            }
        }
    }
}
