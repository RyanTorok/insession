package gui;

import exceptions.InconsistentSystemTimeException;

import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.sql.Time;
import java.text.SimpleDateFormat;

/**
 * Created by 11ryt on 7/26/2017.
 */
public class Clock extends JLabel {

    public static final long CTMonStartup = System.currentTimeMillis();
    public static final long timeOnStartup = System.nanoTime();
    public static boolean timeDependentAttributesAccessible = true;
    private static long lastOnlineQuery = Long.MAX_VALUE;

    private long startTime;
    private final boolean direction; // true for normal, false for countdown
    private SimpleDateFormat format;
    private long startOffset;
    private boolean started = false;
    private boolean running = false;
    private long elapsedTimeNanos;
    private long previousOffset = 0; //time accumulated from previous runs before pausing.

    public Clock(boolean direction, SimpleDateFormat format) {
        this(direction, format, 0);
    }

    public Clock(boolean direction, SimpleDateFormat format, long startOffset) {
        this.direction = direction;
        this.format = format;
        this.startOffset = startOffset;
        startTime = Long.MIN_VALUE;
    }



    private void start() {
        startTime = System.nanoTime();
        started = true;
        running = true;
    }

    private void updateOffset() {
        if (running)
            elapsedTimeNanos = System.nanoTime() - startTime;
    }

    public void update() {
        updateOffset();
        setText(convertLongToDisplayTime());
    }

    private String convertLongToDisplayTime() {
        return format.format(new Time((direction) ? startOffset + elapsedTimeNanos : startOffset - elapsedTimeNanos));
    }

    public void pause() {
        updateOffset();
        running = false;
    }

    public void resume() {
        previousOffset = elapsedTimeNanos;
        startTime = System.nanoTime();
        running = true;
        updateOffset();
    }

}
