package ru.ok.timer.util;

import java.util.Locale;

public final class Timers {

    public static final long TIMER_LENGTH_IN_MILLIS = 60_000;
    public static final int TIMER_ID = 73;

    // Suppress default constructor for noninstantiability
    private Timers() {
        throw new AssertionError();
    }

    public static String formattedTimeWithMillis(final long timeInMillis) {
        final int minutes = (int) (timeInMillis / 1000) / 60;
        final int seconds = (int) (timeInMillis / 1000) % 60;
        final int millis  = (int) (timeInMillis % 10_000);
        return  String.format(Locale.getDefault(),
                "%02d:%02d:%04d", minutes, seconds, millis);
    }

    public static String formattedTime(final long timeInMillis) {
        final int minutes = (int) (timeInMillis / 1000) / 60;
        final int seconds = (int) (timeInMillis / 1000) % 60;
        return  String.format(Locale.getDefault(),
                "%02d:%02d", minutes, seconds);
    }

    public enum TimerState {
        STOPPED,
        PAUSED,
        RUNNING
    }

    public enum TimerActions {
        START,
        PAUSE,
        RESET
    }
}
