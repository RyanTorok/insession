package gui;

import classes.RecordEntry;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import main.Root;

import java.util.List;
import java.util.stream.Collectors;

public class Calendar extends TaskView {

    public Calendar() {
        super("Calendar");
        List<RecordEntry> records = Root.getActiveUser().getUpdates().stream().map(record -> record.latest()).collect(Collectors.toList());
    }

    public Pane getFullDisplay() {
                        return null;
    }

    @Override
    ImageView getPlaceholderImage() {
        return null;
    }

    @Override
    protected Pane initDisplay() {
        return null;
    }
}
