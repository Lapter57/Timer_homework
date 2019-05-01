package ru.ok.timer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import ru.ok.timer.services.TimerService;
import ru.ok.timer.util.TimerPreferences;

import static ru.ok.timer.util.Timers.TIMER_LENGTH_IN_MILLIS;
import static ru.ok.timer.util.Timers.TimerState;
import static ru.ok.timer.util.Timers.formattedTimeWithMillis;

public class MainActivity extends AppCompatActivity {

    private TextView timerTextView;
    private Button startStopButton;
    private Button resetButton;

    private CountDownTimer timer;
    private TimerState timerState;
    private long timeLeftInMillis;

    private Intent timerServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerTextView = findViewById(R.id.timer);
        startStopButton = findViewById(R.id.btn_start_stop);
        resetButton = findViewById(R.id.btn_reset);

        timerServiceIntent = new Intent(this, TimerService.class);

        startStopButton.setOnClickListener((v) -> {
            if (timerState == TimerState.RUNNING) {
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
        TimerPreferences.setTimerState(timerState, this);
        TimerPreferences.setTimeLeft(timeLeftInMillis, this);
        startService(timerServiceIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService(timerServiceIntent);
        initTimer();
    }

    private void initTimer() {
        timerState = TimerPreferences.getTimerState(this);
        timeLeftInMillis = TimerPreferences.getTimeLeft(this);
        switch (timerState) {
            case RUNNING:
                startTimer();
                break;

            case PAUSED:
                if (timer != null) {
                    timer.cancel();
                }
                break;

            case STOPPED:
                if (timer != null) {
                    timer.cancel();
                }
                startStopButton.setText(R.string.start);
                resetButton.setEnabled(true);
                break;
        }
        updateTimerView();
    }

    private void startTimer() {
        final Context context = this;
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
                TimerPreferences.setDefaultPreferences(context);
                startStopButton.setText(R.string.start);
                resetButton.setEnabled(true);
                updateTimerView();
            }
        }.start();

        timerState = TimerState.RUNNING;
        startStopButton.setText(R.string.pause);
        resetButton.setEnabled(false);
    }

    private void pauseTimer() {
        timer.cancel();
        timerState = TimerState.PAUSED;
        TimerPreferences.setTimerState(timerState, this);
        TimerPreferences.setTimeLeft(timeLeftInMillis, this);
        startStopButton.setText(R.string.start);
        resetButton.setEnabled(true);
    }

    private void resetTimer() {
        if (timerState != TimerState.STOPPED) {
            if (timer != null) {
                timer.cancel();
            }
            timerState = TimerState.STOPPED;
            timeLeftInMillis = TIMER_LENGTH_IN_MILLIS;
            TimerPreferences.setDefaultPreferences(this);
            startStopButton.setText(R.string.start);
            updateTimerView();
        }
    }

    private void updateTimerView() {
        timerTextView.setText(formattedTimeWithMillis(timeLeftInMillis));
    }
}
