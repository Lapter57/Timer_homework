package ru.ok.timer.util;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import ru.ok.timer.MainActivity;
import ru.ok.timer.receivers.TimerActionReceiver;

import ru.ok.timer.R;
import ru.ok.timer.services.CountDownService;

public final class TimerNotifications {

    private static final int TIMER_ID = 0;
    private static final String TIMER_CHANNEL_ID = "ru.ok.timer.timerChannel";
    private static final String TIMER_CHANNEL_NAME = "Techno Timer";

    // Suppress default constructor for noninstantiability
    private TimerNotifications() {
        throw new AssertionError();
    }

    public static void showTimerRunningNotification(@NonNull final Context context,
                                                    @NonNull final long timeLeft) {
        final PendingIntent stopPendingIntent = getPendingIntent(TimerActions.STOP, context);
        final NotificationCompat.Builder builder = getNotificationBuilder(context);
        builder.setContentIntent(getPendingIntentWithStack(context, MainActivity.class))
               .setContentText(MainActivity.formattedTime(timeLeft))
               .addAction(R.drawable.ic_pause, "Stop", stopPendingIntent);
        showNotification(context, builder);
    }

    public static void showTimerStoppedNotification(@NonNull final Context context) {
        final PendingIntent startPendingIntent = getPendingIntent(TimerActions.START, context);
        final PendingIntent resetPendingIntent = getPendingIntent(TimerActions.RESET, context);
        final NotificationCompat.Builder builder = getNotificationBuilder(context);
        builder.setContentText("Timer paused")
               .setContentIntent(getPendingIntentWithStack(context, MainActivity.class))
               .addAction(R.drawable.ic_start, "Start", startPendingIntent)
               .addAction(R.drawable.ic_reset, "Reset", resetPendingIntent);
        showNotification(context, builder);
    }

    public static void showTimerExpiredNotification(@NonNull final Context context) {
        final NotificationCompat.Builder builder = getNotificationBuilder(context);
        builder.setContentText("Timer expired")
               .setContentIntent(getPendingIntentWithStack(context, MainActivity.class));
        showNotification(context, builder);
    }

    public static void removeNotification(@NonNull final Context context) {
        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(TIMER_ID);
        }
    }

    public static void setAlarm(@NonNull final Context context,
                                @NonNull final long wakeUpTime) {
        final AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        final PendingIntent pendingIntent = getAlarmPendingIntent(context);

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent);
            Intent serviceIntent = new Intent(context, CountDownService.class);
            context.startService(serviceIntent);
        }
    }

    public static void removeAlarm(@NonNull final Context context) {
        final AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        final PendingIntent pendingIntent = getAlarmPendingIntent(context);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Intent serviceIntent = new Intent(context, CountDownService.class);
            context.stopService(serviceIntent);
        }
    }

    @NonNull
    private static PendingIntent getAlarmPendingIntent(@NonNull final Context context) {
        final Intent intent = new Intent(context, TimerActionReceiver.class);
        intent.setAction(TimerActions.EXPIRED.toString());
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @NonNull
    private static PendingIntent getPendingIntent(@NonNull final TimerActions timerAction,
                                                  @NonNull final Context context) {
        final Intent intent = new Intent(context, TimerActionReceiver.class);
        intent.setAction(timerAction.toString());
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @NonNull
    private static NotificationCompat.Builder getNotificationBuilder(
            @NonNull final Context context) {
        final NotificationCompat.Builder builder = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ? new NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
                : new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_timer)
               .setAutoCancel(true)
               .setContentTitle("Timer");
        return builder;
    }

    @NonNull
    private static <T> PendingIntent getPendingIntentWithStack(@NonNull final Context context,
                                                               @NonNull final Class<T> clazz) {
        final Intent resultIntent = new Intent(context, clazz);
        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(clazz);
        stackBuilder.addNextIntent(resultIntent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static void showNotification(@NonNull final Context context,
                                         @NonNull final NotificationCompat.Builder builder) {
        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final int importance = NotificationManager.IMPORTANCE_DEFAULT;
            final NotificationChannel channel = new NotificationChannel(
                    TIMER_CHANNEL_ID,
                    TIMER_CHANNEL_NAME,
                    importance);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        if (notificationManager != null) {
            notificationManager.notify(TIMER_ID, builder.build());
        }
    }

    public enum TimerActions {
        START,
        STOP,
        RESET,
        EXPIRED
    }
}
