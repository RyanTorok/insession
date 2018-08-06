package gui;

import classes.ClassPd;
import filesystem.FileSystemElement;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import main.UtilAndConstants;

public class SidebarHotLink extends HBox {

    private final Text text;
    private String name;
    private FileSystemElement target;
    private boolean active;

    public SidebarHotLink(ClassPd wrapper, String name, FileSystemElement target) {
        this.name = name;
        this.target = target;
        text = new Text(name);
        getChildren().add(text);
        setStyle("-fx-background-color: " + UtilAndConstants.colorToHex(UtilAndConstants.highlightColor(wrapper.getColor())));
        UtilAndConstants.highlightOnMouseOver(this);
    }

    public boolean isActive() {
        return active;
    }
}
