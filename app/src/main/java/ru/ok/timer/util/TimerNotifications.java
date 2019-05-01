package ru.ok.timer.util;

import android.app.Notification;
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
import ru.ok.timer.R;
import ru.ok.timer.receivers.TimerActionReceiver;

import static ru.ok.timer.util.Timers.TIMER_ID;
import static ru.ok.timer.util.Timers.TimerActions;
import static ru.ok.timer.util.Timers.TimerState;
import static ru.ok.timer.util.Timers.formattedTime;

public final class TimerNotifications {

    private static final String TIMER_CHANNEL_ID = "ru.ok.timer.timerChannel";
    private static final String TIMER_CHANNEL_NAME = "Techno Timer";

    // Suppress default constructor for noninstantiability
    private TimerNotifications() {
        throw new AssertionError();
    }

    public static Notification createNotification(
            @NonNull final Context context,
            @NonNull final long timeLeft,
            @NonNull final TimerState timerState) {
        final Notification notification =
                createTimerStateNotification(context, timeLeft, timerState);
        if (notification != null) {
            creatNotificationChannel(context);
        }
        return notification;
    }

    public static void updateNotification(
            @NonNull final Context context,
            @NonNull final long timeLeft,
            @NonNull final TimerState timerState) {
        final Notification notification =
                createTimerStateNotification(context, timeLeft, timerState);
        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(TIMER_ID, notification);
        }
    }

    public static void removeNotification(@NonNull final Context context) {
        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(TIMER_ID);
        }
    }

    private static Notification createTimerStateNotification(
            @NonNull final Context context,
            @NonNull final long timeLeft,
            @NonNull final TimerState timerState) {
        switch (timerState) {
            case RUNNING:
                return createTimerRunningNotification(context, timeLeft);
            case PAUSED:
                return createTimerPausedNotification(context);
            case STOPPED:
                return createTimerExpiredNotification(context);
        }
        return null;
    }

    @NonNull
    private static Notification createTimerRunningNotification(
            @NonNull final Context context,
            @NonNull final long timeLeft) {
        final PendingIntent stopPendingIntent = getPendingIntent(TimerActions.PAUSE, context);
        final NotificationCompat.Builder builder = getNotificationBuilder(context);
        builder.setContentIntent(getPendingIntentWithStack(context, MainActivity.class))
               .setContentText(formattedTime(timeLeft))
               .addAction(R.drawable.ic_pause, "Pause", stopPendingIntent);
        return builder.build();
    }

    @NonNull
    private static Notification createTimerPausedNotification(@NonNull final Context context) {
        final PendingIntent startPendingIntent = getPendingIntent(TimerActions.START, context);
        final PendingIntent resetPendingIntent = getPendingIntent(TimerActions.RESET, context);
        final NotificationCompat.Builder builder = getNotificationBuilder(context);
        builder.setContentText("Timer paused")
               .setContentIntent(getPendingIntentWithStack(context, MainActivity.class))
               .addAction(R.drawable.ic_start, "Start", startPendingIntent)
               .addAction(R.drawable.ic_reset, "Reset", resetPendingIntent);
        return builder.build();
    }

    @NonNull
    private static Notification createTimerExpiredNotification(@NonNull final Context context) {
        final NotificationCompat.Builder builder = getNotificationBuilder(context);
        builder.setContentText("Timer expired")
               .setContentIntent(getPendingIntentWithStack(context, MainActivity.class));
        return builder.build();
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
    private static <T> PendingIntent getPendingIntentWithStack(
            @NonNull final Context context,
            @NonNull final Class<T> clazz) {
        final Intent resultIntent = new Intent(context, clazz);
        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(clazz);
        stackBuilder.addNextIntent(resultIntent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static void creatNotificationChannel(@NonNull final Context context) {
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
    }
}
