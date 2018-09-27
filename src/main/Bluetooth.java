package main;

import gui.ConfirmableTask;
import gui.PopupNotification;

/**
 * Created by 11ryt on 4/29/2017.
 */

public class Bluetooth {



    private boolean markPresent(classes.ClassPd activeClass, Student s){
       return s.markPresent(activeClass, new java.sql.Time(System.currentTimeMillis()));
    }

    public void promptConnection() {
        PopupNotification notification = new PopupNotification("Paintbrush needs your permission to use Bluetooth.",
                new ConfirmableTask("Allow", this::connect), new ConfirmableTask("Block", () -> {}));
        Root.getPortal().showNotification(notification);
    }

    private void connect() {

    }
}
