package terminal;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TerminalEventHandler {

    private List<TerminalDrivenEvent> events;

    public TerminalEventHandler(TerminalDrivenEvent... events) {
        this.events = Arrays.asList(events);
    }

    public TerminalEventHandler(List<TerminalDrivenEvent> events) {
        this.events = events;
    }

    public TerminalEventHandler() {events = new ArrayList<>(); }

    public void run() {
        for (TerminalDrivenEvent event : events) {
            event.fire();
        }
    }

    public List<TerminalDrivenEvent> getEvents() {
        return events;
    }
}
