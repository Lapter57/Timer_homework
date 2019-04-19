package ru.ok.timer;

import androidx.appcompat.app.AppCompatActivity;
import ru.ok.timer.util.TimerNotifications;
import ru.ok.timer.util.TimerPreferences;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final long TIMER_LENGTH_IN_MILLIS = 60_000;

    private TextView timerTextView;
    private Button startStopButton;
    private Button resetButton;

    private CountDownTimer timer;
    private TimerState timerState;
    private long timeLeftInMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerTextView = findViewById(R.id.timer);
        startStopButton = findViewById(R.id.btn_start_stop);
        resetButton = findViewById(R.id.btn_reset);

        startStopButton.setOnClickListener((v) -> {
            if (timerState.equals(TimerState.RUNNING)) {
                pauseTimer();
            } else {
                startTimer();
            }
        });
        resetButton.setOnClickListener(v -> resetTimer());
    }

    @Override
    protected void onPause() {
        super.onPause();
        final long wakeUpTime = timeLeftInMillis + System.currentTimeMillis();
        TimerPreferences.setTimerState(timerState, this);
        TimerPreferences.setTimeLeft(timeLeftInMillis, this);
        TimerPreferences.setWakeUpTime(wakeUpTime, this);
        switch (timerState) {
            case RUNNING:
                timer.cancel();
                TimerNotifications.setAlarm(this, wakeUpTime);
                TimerNotifications.showTimerRunningNotification(this, timeLeftInMillis);
                break;
            case PAUSED:
                TimerNotifications.showTimerStoppedNotification(this);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initTimer();
        TimerNotifications.removeAlarm(this);
        TimerNotifications.removeNotification(this);
    }

    private void initTimer() {
        timerState = TimerPreferences.getTimerState(this);
        if (!timerState.equals(TimerState.STOPPED)) {
            if (timerState.equals(TimerState.RUNNING)) {
                long wakeUpTime = TimerPreferences.getWakeUpTime(this);
                timeLeftInMillis = wakeUpTime - System.currentTimeMillis();
                startTimer();
            } else if (timerState.equals(TimerState.PAUSED)) {
                timeLeftInMillis = TimerPreferences.getTimeLeft(this);
            }
        } else {
            timeLeftInMillis = TIMER_LENGTH_IN_MILLIS;
            startStopButton.setText(R.string.start);
            resetButton.setEnabled(true);
        }
        updateTimerView();
    }

    private void startTimer() {
        timer = new CountDownTimer(timeLeftInMillis, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerView();
            }

            @Override
            public void onFinish() {
                timerState = TimerState.STOPPED;
                timeLeftInMillis = TIMER_LENGTH_IN_MILLIS;
                startStopButton.setText(R.string.start);
                resetButton.setEnabled(true);
                updateTimerView();
            }
        }.start();

        timerState = TimerState.RUNNING;
        startStopButton.setText(R.string.stop);
        resetButton.setEnabled(false);
    }

    private void pauseTimer() {
        timer.cancel();
        timerState = TimerState.PAUSED;
        startStopButton.setText(R.string.start);
        resetButton.setEnabled(true);
    }

    private void resetTimer() {
        if (!timerState.equals(TimerState.STOPPED)) {
            if (timer != null) {
                timer.cancel();
            }
            timerState = TimerState.STOPPED;
            timeLeftInMillis = TIMER_LENGTH_IN_MILLIS;
            startStopButton.setText(R.string.start);
            updateTimerView();
        }
    }

    private void updateTimerView() {
        timerTextView.setText(formattedTimeWithMillis(timeLeftInMillis));
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
}
