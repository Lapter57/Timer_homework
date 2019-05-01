package ru.ok.timer.services;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;

import ru.ok.timer.util.TimerNotifications;
import ru.ok.timer.util.TimerPreferences;

import static ru.ok.timer.util.Timers.TIMER_ID;
import static ru.ok.timer.util.Timers.TIMER_LENGTH_IN_MILLIS;
import static ru.ok.timer.util.Timers.TimerState;

public class TimerService extends Service {

    private CountDownTimer timer;
    private TimerState timerState;
    private long timeLeftInMillis;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initialize();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
        }
        super.onDestroy();
    }

        @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initialize() {
        timerState = TimerPreferences.getTimerState(this);
        switch (timerState) {
            case RUNNING:
                timeLeftInMillis = TimerPreferences.getTimeLeft(this);
                showNotification();
                startTimer();
                break;

            case PAUSED:
                timeLeftInMillis = TimerPreferences.getTimeLeft(this);
                showNotification();
                break;

            case STOPPED:
                stopSelf();
                break;
        }
    }

    private void showNotification() {
        final Notification notification = TimerNotifications.createNotification(
                this, timeLeftInMillis, timerState);
        startForeground(TIMER_ID, notification);
    }

    public void startTimer() {
        final Context timerContext = this;
        timeLeftInMillis = TimerPreferences.getTimeLeft(timerContext);

        timer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                TimerPreferences.setTimeLeft(timeLeftInMillis, timerContext);
                TimerNotifications.updateNotification(timerContext, timeLeftInMillis, timerState);
            }

            @Override
            public void onFinish() {
                timerState = TimerState.STOPPED;
                timeLeftInMillis = TIMER_LENGTH_IN_MILLIS;
                TimerPreferences.setDefaultPreferences(timerContext);
                TimerNotifications.updateNotification(timerContext, timeLeftInMillis, timerState);
            }
        }.start();
    }
}
