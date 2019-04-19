package ru.ok.timer.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import ru.ok.timer.MainActivity;
import ru.ok.timer.util.TimerNotifications;
import ru.ok.timer.util.TimerPreferences;

import ru.ok.timer.R;

public class CountDownService extends IntentService {

    @NonNull
    private CountDownTimer timerUpdatingNotification;

    public CountDownService() {
        super("CountDownService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final Context context = this;
        final long wakeUpTime = TimerPreferences.getWakeUpTime(this);
        final long timeLeft = wakeUpTime - System.currentTimeMillis();
        timerUpdatingNotification = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TimerNotifications.showTimerRunningNotification(context, millisUntilFinished);
            }

            @Override
            public void onFinish() {
                stopSelf();
            }

        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timerUpdatingNotification.cancel();
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        MainActivity.TimerState timerState;
        do {
            synchronized (this) {
                timerState = TimerPreferences.getTimerState(this);
            }
        } while (!timerState.equals(MainActivity.TimerState.STOPPED));
    }
}
