package ru.ok.timer.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import static ru.ok.timer.util.Timers.TIMER_LENGTH_IN_MILLIS;
import static ru.ok.timer.util.Timers.TimerState;

public final class TimerPreferences {

    private static final String TIMER_STATE_ID = "ru.ok.timer.timerState";
    private static final String TIME_LEFT_ID = "ru.ok.timer.timeLeft";

    // Suppress default constructor for noninstantiability
    private TimerPreferences() {
        throw new AssertionError();
    }

    public static void setTimerState(@NonNull final TimerState timerState,
                                     @NonNull final Context context) {
        final Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        final int ordinal = timerState.ordinal();
        editor.putInt(TIMER_STATE_ID, ordinal);
        editor.apply();
    }

    public static TimerState getTimerState(@NonNull final Context context) {
        final SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        final int ordinal = preferences.getInt(TIMER_STATE_ID, TimerState.STOPPED.ordinal());
        return TimerState.values()[ordinal];
    }

    public static void setTimeLeft(@NonNull final long timeLeft,
                                   @NonNull final Context context) {
        final Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putLong(TIME_LEFT_ID, timeLeft);
        editor.apply();
    }

    @NonNull
    public static long getTimeLeft(@NonNull final Context context) {
        final SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getLong(TIME_LEFT_ID, 0);
    }

    public static void setDefaultPreferences(@NonNull final Context context) {
        setTimerState(TimerState.STOPPED, context);
        setTimeLeft(TIMER_LENGTH_IN_MILLIS, context);
    }
}
