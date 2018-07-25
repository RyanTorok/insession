package terminal;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.Root;

import java.util.function.BooleanSupplier;

public class TerminalDrivenEvent {

    private Trigger trigger;
    private BooleanSupplier freeEvent;

    public static final TerminalDrivenEvent HIDE = new TerminalDrivenEvent(() -> {
        Root.getPortal().quitTerminal();
        return true;
    });

    public static final TerminalDrivenEvent CLEAR = new TerminalDrivenEvent(() -> {
        Root.getPortal().getTerminal().clearTerminal();
        return true;
    });

    public static final TerminalDrivenEvent EXIT = new TerminalDrivenEvent(() -> {
        Root.getPortal().stop();
        return true;
    });

    public TerminalDrivenEvent(BooleanSupplier freeEvent) {
        this.freeEvent = freeEvent;
    }

    public TerminalDrivenEvent(MouseTrigger trigger) {
        this.trigger = trigger;
    }

    public TerminalDrivenEvent(KeyTrigger trigger) {
        this.trigger = trigger;
    }

    public TerminalDrivenEvent(ActionTrigger trigger) {
        this.trigger = trigger;
    }

    public boolean fire() {
        if (freeEvent != null && trigger != null)
            throw new IllegalStateException("Multiple events defined for command.");
        if (freeEvent == null && trigger == null)
            throw new IllegalStateException("No event or incomplete event defined for command.");
        if (freeEvent != null) {
            return freeEvent.getAsBoolean();
        } else {
            Timeline delay = new Timeline(new KeyFrame(Duration.millis(0)));
            delay.setOnFinished(event -> {
                if (trigger != null) {
                    if (trigger.node instanceof Stage)
                        ((Stage) trigger.node).fireEvent(trigger.getEvent());
                    else if (trigger.node instanceof Node)
                        ((Node) trigger.node).fireEvent(trigger.getEvent());
                    else throw new IllegalArgumentException("Event target must be Stage or Node");
                }
                else throw new IllegalStateException("Null event trigger encountered.");
            });
            delay.play();
        }
        return true;
    }

    static abstract class Trigger {
        private final EventTarget node;

        public Trigger(EventTarget node) {
            this.node = node;
        }

        abstract Event getEvent();
    }

     static class KeyTrigger extends Trigger {

        private final EventType<KeyEvent> eventType;
        private final KeyCode code;
        private final KeyEvent event;

        public KeyTrigger(EventTarget node, EventType<KeyEvent> eventType, KeyCode code) {
            super(node);
            this.eventType = eventType;
            this.code = code;
            event = new KeyEvent(null, node, eventType, code.getChar(), code.getChar(), code, false, false, false, false);
        }

         public EventType<KeyEvent> getEventType() {
             return eventType;
         }

         public KeyCode getCode() {
             return code;
         }

         public KeyEvent getEvent() {
             return event;
         }
     }

    static class MouseTrigger extends Trigger {

        private final EventType<MouseEvent> type;
        private final MouseEvent event;

        public MouseTrigger(EventTarget node, EventType<MouseEvent> type) {
            super(node);
            this.type = type;
            event = new MouseEvent(type, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null);
        }

        @Override
        Event getEvent() {
            return event;
        }

        public EventType<MouseEvent> getType() {
            return type;
        }
    }

    static class ActionTrigger extends Trigger {
        private final ActionEvent event;


        ActionTrigger(EventTarget node) {
            super(node);
            event = new ActionEvent(null, node);
        }

        @Override
        Event getEvent() {
            return event;
        }
    }

}
