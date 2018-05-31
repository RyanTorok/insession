package gui;

import classes.SQL;

import javax.swing.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Created by 11ryt on 8/18/2017.
 */
public class RecentUpdate {

    private static ArrayList<RecentUpdate> allUpdates = new ArrayList<RecentUpdate>();

    public static ArrayList<RecentUpdate> getAllUpdates() {
        if(!updateServer()){
            new Exception("Database Error Thrown when attempting to update recent update server.").printStackTrace();
        }
        try {
            return (ArrayList) allUpdates.clone();
        } catch (ClassCastException e){
            e.printStackTrace();
            return new ArrayList<RecentUpdate>();
        }
    }
    private static boolean updateServer(){
        try{
            allUpdates = (ArrayList<RecentUpdate>)((ArrayList) SQL.updateServer().get(0)).clone();
            return true;
        } catch (SQLException e){
            return false;
        }
        catch (ClassCastException e){
            e.printStackTrace();
            return false;
        }
        catch(NullPointerException e){
            return false;
        }
    }

    public RecentUpdate(classes.Record r, Timestamp time){

    }

    public JComponent toJComponent() {
        return null;
    }
}
