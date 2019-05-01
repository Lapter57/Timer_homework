package ru.ok.timer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.ok.timer.services.TimerService;
import ru.ok.timer.util.TimerNotifications;
import ru.ok.timer.util.TimerPreferences;

import static ru.ok.timer.util.Timers.TimerActions;
import static ru.ok.timer.util.Timers.TimerState;

public class TimerActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent.getAction() != null) {
            final String action = intent.getAction();
            final TimerActions timerActions = TimerActions.valueOf(action);
            final Intent serviceIntent = new Intent(context, TimerService.class);
            switch (timerActions) {
                case START:
                    TimerPreferences.setTimerState(TimerState.RUNNING, context);
                    context.startService(serviceIntent);
                    break;

                case PAUSE:
                    context.stopService(serviceIntent);
                    final long timeLeft = TimerPreferences.getTimeLeft(context);
                    TimerPreferences.setTimerState(TimerState.PAUSED, context);
                    TimerNotifications.updateNotification(context,
                            timeLeft, TimerState.PAUSED);
                    break;

                case RESET:
                    context.stopService(serviceIntent);
                    TimerPreferences.setDefaultPreferences(context);
                    TimerNotifications.removeNotification(context);
                    break;

            }
        }
    }
}
