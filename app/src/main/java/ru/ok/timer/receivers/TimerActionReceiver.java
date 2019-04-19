package ru.ok.timer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.ok.timer.util.TimerNotifications;
import ru.ok.timer.util.TimerPreferences;

import static ru.ok.timer.MainActivity.TimerState;
import static ru.ok.timer.util.TimerNotifications.TimerActions.EXPIRED;
import static ru.ok.timer.util.TimerNotifications.TimerActions.RESET;
import static ru.ok.timer.util.TimerNotifications.TimerActions.START;
import static ru.ok.timer.util.TimerNotifications.TimerActions.STOP;

public class TimerActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent.getAction() != null) {
            final String action = intent.getAction();

            if (action.equals(START.toString())) {
                final long timeLeft = TimerPreferences.getTimeLeft(context);
                final long wakeUpTime = System.currentTimeMillis() + timeLeft;
                TimerPreferences.setTimerState(TimerState.RUNNING, context);
                TimerPreferences.setWakeUpTime(wakeUpTime, context);
                TimerNotifications.setAlarm(context, wakeUpTime);
                TimerNotifications.showTimerRunningNotification(context, timeLeft);

            } else if (action.equals(STOP.toString())) {
                final long wakeUpTime = TimerPreferences.getWakeUpTime(context);
                final long timeLeft = wakeUpTime - System.currentTimeMillis();
                TimerPreferences.setTimeLeft(timeLeft, context);
                TimerPreferences.setTimerState(TimerState.PAUSED, context);
                TimerNotifications.removeAlarm(context);
                TimerNotifications.showTimerStoppedNotification(context);

            } else if (action.equals(RESET.toString())) {
                TimerPreferences.setDefaultPreferences(context);
                TimerNotifications.removeAlarm(context);
                TimerNotifications.removeNotification(context);

            } else if (action.equals(EXPIRED.toString())) {
                TimerPreferences.setDefaultPreferences(context);
                TimerNotifications.removeAlarm(context);
                TimerNotifications.showTimerExpiredNotification(context);
            }
        }
    }
}
