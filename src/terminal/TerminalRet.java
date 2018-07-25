package terminal;

public class TerminalRet {
    private String text;
    private TerminalEventHandler events;

    public TerminalRet(String text) {
        this.text = text;
        events = new TerminalEventHandler();
    }

    public TerminalRet(String text, TerminalDrivenEvent... events) {
        this.text = text;
        this.events = new TerminalEventHandler(events);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public TerminalEventHandler getEvents() {
        return events;
    }
}
