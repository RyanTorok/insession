package gui;

import classes.Record;
import classes.RecordEntry;
import main.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Calendar extends TaskView {

    public Calendar() {
        super("Calendar");
        List<RecordEntry> records = Root.getActiveUser().getUpdates().stream().map(record -> record.latest()).collect(Collectors.toList());
    }
}
